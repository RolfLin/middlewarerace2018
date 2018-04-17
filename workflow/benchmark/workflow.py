import datetime
import os
import re
import subprocess
import time

from functools import partial
from logging import getLogger
from pathlib import Path
from shlex import split

from benchmark.model.workspace import Workspace


LOGS_FILE_NAME = 'logs.tar.gz'

FAILED_TO_LOCK_LOCAL_WORKSPACE = 1010
FAILED_TO_GENERATE_DOCKER_PASSWORD_FILE = 1020
FAILED_TO_CREATE_REMOTE_TASK_HOME = 1030
FAILED_TO_LOCK_REMOTE_TASK_HOME = 1040
FAILED_TO_UPLOAD_DOCKER_PASSWORD_FILE = 1050
FAILED_TO_LOGIN_TO_DOCKER_REPOSITORY = 1060
FAILED_TO_PULL_DOCKER_IMAGE = 1070
FAILED_TO_START_ETCD_SERVICE = 1080
FAILED_TO_START_PROVIDER_SERVICES = 1090
FAILED_TO_START_CONSUMER_SERVICE = 1100
FAILED_TO_WARMUP_APPLICATIONS = 1110
FAILED_TO_PRESSURE_APPLICATIONS = 1120

VALID_ERRORS = [
    FAILED_TO_LOGIN_TO_DOCKER_REPOSITORY,
    FAILED_TO_PULL_DOCKER_IMAGE,
    FAILED_TO_START_PROVIDER_SERVICES,
    FAILED_TO_START_CONSUMER_SERVICE
]


class Workflow():

    def __init__(self, config, task):
        self.config = config
        self.task = task
        self.logger = getLogger(__name__)
        self.workspace = Workspace(self.config, self.task)
        self.qps_pattern = re.compile(
            '^Requests/sec:\s*(\d*\.\d*)', re.M | re.I)
        self.qps_results = {}
        self.best_qps = -1
        self.best_scale = -1

        self.logger.info('local workspace = %s', self.workspace.local)
        self.logger.info('remote workspace = %s', self.workspace.remote)

    def run(self):
        start = time.time()

        try:
            self.__lock_local_workspace()
            self.__generate_dockerpwd_file()
            self.__create_remote_task_home()
            self.__lock_remote_task_home()
            self.__upload_dockerpwd_file()
            self.__docker_login()
            self.__pull_docker_image()
            self.__start_etcd()
            self.__start_providers()
            self.__start_consumer()
            self.__warmup_then_pressure()

            result = {
                'status': 0,
                'is_valid': 1,
                'message': 'Success',
                'rank': self.best_qps
            }
        except WorkflowError as err:
            result = {
                'status': -err.error_code,
                'is_valid': 1 if err.error_code in VALID_ERRORS else 0,
                'message': err.message,
                'rank': self.best_qps
            }
            self.logger.exception('Failed to execute workflow.')
        finally:
            self.__stop_services()
            self.__cleanup()
            self.__collect_data()

        end = time.time()
        self.logger.info(
            'Time used: %s',
            datetime.timedelta(seconds=(end - start)))

        return result

    def __lock_local_workspace(self):
        self.logger.info('>>> Lock local workspace.')

        local = self.workspace.local
        try:
            Path(local.lock_file).touch(exist_ok=False)
        except FileExistsError as err:
            raise WorkflowError(
                'Failed to lock local workspace due to lock file exists.',
                error_code=FAILED_TO_LOCK_LOCAL_WORKSPACE) from err
        except Exception as err:
            raise WorkflowError(
                'Failed to lock local workspace.',
                error_code=FAILED_TO_LOCK_LOCAL_WORKSPACE) from err

    def __generate_dockerpwd_file(self):
        self.logger.info('>>> Generate Docker password file.')

        password = self.task.docker_password
        dockerpwd_file = self.workspace.local.dockerpwd_file
        try:
            Path(dockerpwd_file).write_text(password)
        except Exception as err:
            raise WorkflowError(
                'Failed to generate Docker password file.',
                error_code=FAILED_TO_GENERATE_DOCKER_PASSWORD_FILE) from err

    def __create_remote_task_home(self):
        self.logger.info('>>> Create remote task home.')

        remote = self.workspace.remote
        script = """
            mkdir -p {ws.task_home}
            exit 0
        """.format(ws=remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to create remote task home.',
                error_code=FAILED_TO_CREATE_REMOTE_TASK_HOME)

    def __lock_remote_task_home(self):
        self.logger.info('>>> Lock remote task home.')

        remote = self.workspace.remote
        script = """
            if [[ -f {ws.lock_file} ]]; then
                echo "Lock file exists."
                exit 1
            else
                touch {ws.lock_file}
            fi
            exit 0
        """.format(ws=remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode == 0:
            return
        if returncode == 1:
            raise WorkflowError(
                'Failed to lock remote task home due to lock file exists.',
                error_code=1041)
        raise WorkflowError(
            'Failed to lock remote task home.',
            error_code=FAILED_TO_LOCK_REMOTE_TASK_HOME)

    def __upload_dockerpwd_file(self):
        self.logger.info('>>> Upload Docker password file.')

        local = self.workspace.local
        remote = self.workspace.remote
        script = """
            if [[ -f {local.dockerpwd_file} ]]; then
                scp -q \
                    {local.dockerpwd_file} \
                    {remote.user}@{remote.hostname}:{remote.dockerpwd_file}
                rm -f {local.dockerpwd_file}
            else
                echo "Docker password file not exists."
                exit 1
            fi
            exit 0
        """.format(local=local, remote=remote).rstrip()

        returncode, outs, _ = self.__run_local_script(script)
        if returncode == 0:
            return
        if returncode == 1:
            raise WorkflowError(
                'Failed to upload Docker password file due to file not exists.',  # noqa: E501
                error_code=1051)
        raise WorkflowError(
            'Failed to upload Docker password file.',
            error_code=FAILED_TO_UPLOAD_DOCKER_PASSWORD_FILE)

    def __docker_login(self):
        self.logger.info('>>> Login to Docker repository.')

        script = """
            cat ~/.passwd | sudo -S -p '' docker login \
                -u {task.docker_username} \
                -p $(cat {ws.dockerpwd_file}) \
                {task.docker_host}
            rm -f {ws.dockerpwd_file}
            exit 0
        """.format(task=self.task, ws=self.workspace.remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to login to Docker repository.',
                error_code=FAILED_TO_LOGIN_TO_DOCKER_REPOSITORY)

    def __pull_docker_image(self):
        self.logger.info('>>> Pull Docker image.')

        script = """
            cat ~/.passwd | sudo -S -p '' docker pull {}
            exit 0
        """.format(self.task.image_path).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to pull Docker image.',
                error_code=FAILED_TO_PULL_DOCKER_IMAGE)

    def __start_etcd(self):
        self.logger.info('>>> Start etcd service.')

        script = """
            # noqa: E501

            ETCD_HOME={ws.task_home}/etcd
            IP_ADDR=$(ip addr show docker0 | grep 'inet\\b' | awk '{{print $2}}' | cut -d '/' -f 1)
            PORT={port}
            CLIENT_URL=http://$IP_ADDR:$PORT

            rm -rf $ETCD_HOME
            mkdir -p $ETCD_HOME
            nohup /opt/etcd/etcd \
                  --listen-client-urls $CLIENT_URL \
                  --advertise-client-urls $CLIENT_URL \
                  --data-dir $ETCD_HOME/data > $ETCD_HOME/etcd.log 2>&1 &
            echo $! > $ETCD_HOME/run.pid

            ATTEMPTS=0
            MAX_ATTEMPTS=10
            while true; do
                echo "Trying to connect $IP_ADDR:$PORT..."
                nc -v -n -w 1 --send-only $IP_ADDR $PORT < /dev/null
                if [[ $? -eq 0 ]]; then
                    exit 0
                fi
                if [[ $ATTEMPTS -eq $MAX_ATTEMPTS ]]; then
                    echo "Cannot connect to port $PORT after $ATTEMPTS attempts."
                    exit 1
                fi
                ATTEMPTS=$((ATTEMPTS+1))
                echo "Waiting for 5 seconds... ($ATTEMPTS/$MAX_ATTEMPTS)"
                sleep 5
            done
        """.format(
            ws=self.workspace.remote,
            port=self.config.etcd_port).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to start etcd service.',
                error_code=FAILED_TO_START_ETCD_SERVICE)

    def __start_providers(self):
        self.logger.info('>>> Start provider services.')

        template = """
            cd {ws.task_home}
            if [[ -d provider-{scale} ]]; then
                rm -rf provider-{scale}
            fi
            mkdir -p provider-{scale}/logs
            cd provider-{scale}
            cat ~/.passwd | sudo -S -p '' docker run -d \
                --name provider-{scale} \
                --cidfile run.cid \
                --cpu-period {period} \
                --cpu-quota {quota} \
                -m {memory} \
                --network host \
                -v {ws.task_home}/provider-{scale}/logs:/root/logs \
                {task.image_path} provider-{scale}
        """.rstrip()
        remote = self.workspace.remote
        task = self.task
        script = ''
        script += template.format(
            ws=remote,
            scale='small',
            period=self.config.cpu_period,
            quota=self.config.small_provider_cpu_quota,
            memory=self.config.small_provider_memory,
            task=task)
        script += template.format(
            ws=remote,
            scale='medium',
            period=self.config.cpu_period,
            quota=self.config.medium_provider_cpu_quota,
            memory=self.config.medium_provider_memory,
            task=task)
        script += template.format(
            ws=remote,
            scale='large',
            period=self.config.cpu_period,
            quota=self.config.large_provider_cpu_quota,
            memory=self.config.large_provider_memory,
            task=task)
        script += """
            # noqa: E501

            ATTEMPTS=0
            MAX_ATTEMPTS=10
            while true; do
                echo "Trying to connect 127.0.0.1:{port1}..."
                nc -v -n -w 1 --send-only 127.0.0.1 {port1} < /dev/null; r1=$?

                echo "Trying to connect 127.0.0.1:{port2}..."
                nc -v -n -w 1 --send-only 127.0.0.1 {port2} < /dev/null; r2=$?

                echo "Trying to connect 127.0.0.1:{port3}..."
                nc -v -n -w 1 --send-only 127.0.0.1 {port3} < /dev/null; r3=$?

                echo $r1, $r2, $r3
                if [[ $r1 -eq 0 && $r2 -eq 0 && $r3 -eq 0 ]]; then
                    exit 0
                fi
                if [[ $ATTEMPTS -eq $MAX_ATTEMPTS ]]; then
                    echo "Cannot connect to some of the ports {port1}, {port2}, {port3} after $ATTEMPTS attempts."
                    exit 1
                fi
                ATTEMPTS=$((ATTEMPTS+1))
                echo "Waiting for 5 seconds... ($ATTEMPTS/$MAX_ATTEMPTS)"
                sleep 5
            done
        """.format(
            port1=self.config.small_provider_port,
            port2=self.config.medium_provider_port,
            port3=self.config.large_provider_port).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to start provider services.',
                error_code=FAILED_TO_START_PROVIDER_SERVICES)

    def __start_consumer(self):
        self.logger.info('>>> Start consumer service.')

        script = """
            # noqa: E501

            cd {ws.task_home}
            if [[ -d consumer ]]; then
                rm -rf consumer
            fi
            mkdir -p consumer/logs
            cd consumer
            cat ~/.passwd | sudo -S -p '' docker run -d \
                --name consumer \
                --cidfile run.cid \
                --cpu-period {period} \
                --cpu-quota {quota} \
                -m {memory} \
                --network host \
                -v {ws.task_home}/consumer/logs:/root/logs \
                {task.image_path} consumer

            ATTEMPTS=0
            MAX_ATTEMPTS=10
            while true; do
                echo "Trying to connect 127.0.0.1:{port}..."
                nc -v -n -w 1 --send-only 127.0.0.1 {port} < /dev/null
                if [[ $? -eq 0 ]]; then
                    exit 0
                fi
                if [[ $ATTEMPTS -eq $MAX_ATTEMPTS ]]; then
                    echo "Cannot connect to port {port} after $ATTEMPTS attempts."
                    exit 1
                fi
                ATTEMPTS=$((ATTEMPTS+1))
                echo "Waiting for 5 seconds... ($ATTEMPTS/$MAX_ATTEMPTS)"
                sleep 5
            done
        """.format(
            ws=self.workspace.remote,
            period=self.config.cpu_period,
            quota=self.config.consumer_cpu_quota,
            memory=self.config.consumer_memory,
            task=self.task,
            port=self.config.consumer_port).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to start consumer service.',
                error_code=FAILED_TO_START_CONSUMER_SERVICE)

    def __warmup_then_pressure(self):
        template = """
            sleep {sleep}
            wrk -t{threads} -c{connections} -d{duration} -T{timeout} \
                --latency http://{hostname}:{port}/invoke
            exit 0
        """.rstrip()
        tpl = partial(
            template.format,
            timeout=self.config.wrk_timeout,
            hostname=self.workspace.remote.hostname,
            port=self.config.consumer_port)

        self.logger.info('>>> Warmup.')
        script = ''
        script += tpl(
            sleep=5,
            threads=self.config.wrk_threads,
            connections=self.config.large_scale,
            duration=self.config.warmup_duration)

        returncode, outs, _ = self.__run_local_script(script)
        if returncode != 0:
            raise WorkflowError(
                'Failed to warmup applications.',
                error_code=FAILED_TO_WARMUP_APPLICATIONS)

        for scale in [self.config.small_scale,
                      self.config.medium_scale,
                      self.config.large_scale]:
            self.logger.info('>>> Pressure with %s connections.', scale)
            script = ''
            script += tpl(
                sleep=5, threads=self.config.wrk_threads,
                connections=scale, duration=self.config.pressure_duration)
            returncode, outs, _ = self.__run_local_script(script)
            if returncode != 0:
                raise WorkflowError(
                    'Failed to pressure applications with {} connections.'.format(scale),  # noqa: E501
                    error_code=FAILED_TO_PRESSURE_APPLICATIONS)

            qps = self.__extract_qps(outs)
            self.logger.info('QPS = %s', qps)
            self.qps_results[scale] = qps

    def __extract_qps(self, outs):
        match = self.qps_pattern.search(outs)
        if match is None:
            return -1
        return float(match.group(1))

    def __stop_services(self):
        self.__stop_consumer()
        self.__stop_providers()
        self.__stop_etcd()

    def __stop_consumer(self):
        self.logger.info('>>> Stop consumer service.')

        script = """
            CID_FILE={ws.task_home}/consumer/run.cid
            CID=$(cat $CID_FILE)
            cat ~/.passwd | sudo -S -p '' docker stop $CID
            cat ~/.passwd | sudo -S -p '' docker rm $CID
            rm -f $CID_FILE
            exit 0
        """.format(ws=self.workspace.remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to stop consumer service.')

    def __stop_providers(self):
        self.logger.info('>>> Stop provider services.')

        template = """
            CID_FILE={ws.task_home}/provider-{scale}/run.cid
            CID=$(cat $CID_FILE)
            cat ~/.passwd | sudo -S -p '' docker stop $CID
            cat ~/.passwd | sudo -S -p '' docker rm $CID
            rm -f $CID_FILE
        """.rstrip()
        script = ''
        for scale in ['small', 'medium', 'large']:
            script += template.format(ws=self.workspace.remote, scale=scale)
        script += """
            exit 0
        """.rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to stop provider services.')

    def __stop_etcd(self):
        self.logger.info('>>> Stop etcd service.')

        script = """
            PID_FILE={ws.task_home}/etcd/run.pid
            PID=$(cat $PID_FILE)
            kill -9 $PID
            rm -f $PID_FILE
            exit 0
        """.format(ws=self.workspace.remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to stop etcd service.')

    def __cleanup(self):
        self.__remove_docker_image()
        self.__unlock_remote_task_home()
        self.__unlock_local_task_home()

    def __remove_docker_image(self):
        self.logger.info('>>> Remove Docker image.')

        script = """
            cat ~/.passwd | sudo -S -p '' docker rmi {}
            exit 0
        """.format(self.task.image_path).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to remove Docker image.')

    def __unlock_remote_task_home(self):
        self.logger.info('>>> Unlock remote task home.')

        script = """
            if [[ -f {ws.lock_file} ]]; then
                rm -f {ws.lock_file}
            fi
            exit 0
        """.format(ws=self.workspace.remote).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to unlock remote task home.')

    def __unlock_local_task_home(self):
        self.logger.info('>>> Unlock local workspace.')
        local = self.workspace.local
        try:
            os.remove(local.lock_file)
        except Exception as err:
            self.logger.warn('Failed to unload local workspace. %s', err)

    def __collect_data(self):
        self.logger.info('>>> Collect data.')
        self.__compute_result()
        self.__download_logs()

    def __compute_result(self):
        for scale, qps in self.qps_results.items():
            if qps > self.best_qps:
                self.best_qps = qps
                self.best_scale = scale
        self.logger.info(
            'Best result: %s QPS with %s connections.',
            self.best_qps, self.best_scale)

    def __download_logs(self):
        script = """
            cd {remote.task_home}
            tar -czf ../{file_name} *
            exit 0
        """.format(
            remote=self.workspace.remote,
            file_name=LOGS_FILE_NAME).rstrip()

        returncode, outs, _ = self.__run_remote_script(script)
        if returncode != 0:
            self.logger.warn('Failed to generate logs tarball.')

        script = """
            # noqa: E501

            scp {remote.user}@{remote.hostname}:{remote.task_home}/../{file_name} {local.home}
            echo {task.team_id}/{task.task_id} > {local.home}/.osspath
            exit 0
        """.format(
            remote=self.workspace.remote,
            file_name=LOGS_FILE_NAME,
            local=self.workspace.local,
            task=self.task).rstrip()

        returncode, outs, _ = self.__run_local_script(script)
        if returncode != 0:
            self.logger.warn('Failed to download logs tarball.')

    def __run_local_script(self, script):
        return self.__run_script('bash', script)

    def __run_remote_script(self, script):
        ssh = 'ssh -T -o StrictHostKeyChecking=no {}@{}'.format(
            self.workspace.remote.user, self.workspace.remote.hostname)
        bash = split(ssh) + ['bash']
        return self.__run_script(bash, script)

    def __run_script(self, bash, script):
        self.logger.debug('Script to execute:\n%s\n', script)
        with subprocess.Popen(
            bash,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            encoding='utf-8'
        ) as proc:
            outs, errs = proc.communicate(script)
            returncode = proc.returncode
            self.logger.debug('Return code = %s', returncode)
            self.logger.debug('The output is as following:\n%s', outs)
            return returncode, outs, errs


class WorkflowError(Exception):

    def __init__(self, message, error_code=9999):
        self.message = message
        self.error_code = error_code
