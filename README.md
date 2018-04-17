# 第四界天池中间件性能挑战赛跑分程序

## 一、前期准备

运行此跑分程序，需要准备两台虚拟机，一台用作施压机，一台用作被压机。假设施压机的 hostname 为 `master.tianchi`，则被压机的域名需要是 `<prefix>.master.tianchi`。然后修改 hosts 文件使得从施压机可以通过域名访问被压机。同时需要生成一组密钥对，使得可以从施压机以免密码的形式 `ssh` 到被压机。

### 1.1、准备施压机（以 mscOS 为例）

#### 1.1.1、安装 [Homebrew](https://brew.sh/)

```bash
$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

#### 1.1.2、安装 Python 3

```bash
$ brew install python
```

**注：最新版的 Homebrew 运行 `brew install python` 命令会默认安装 Python 3。**

#### 1.1.3、安装 [Pipenv](https://docs.pipenv.org/)

```bash
$ brew install pipenv
```

#### 1.1.4、克隆本代码仓库

```bash
$ git clone http://gitlab.alibaba-inc.com/albert.tr/tianchi4-benchmarker.git ~/benchmarker
```

#### 1.1.5、创建 Python 运行环境

```bash
$ cd ~/benchmarker/workflow
$ pipenv install
```

#### 1.1.6、安装 wrk

```bash
$ brew install wrk
```

### 1.2、准备被压机（以 CentOS 为例）

#### 1.2.1、安装 Docker

请参考官方文档 [Get Docker CE for CentOS](https://docs.docker.com/install/linux/docker-ce/centos/)。

虽然 Docker 可以以非 root 身份运行，但是本脚本并没有采用这样的运行方式，因此需要使用 `sudo` 运行 `docker` 命令。且我们默认执行 `sudo` 命令的时候是需要输入密码的，因此要在当前用户的 `home` 目录下创建一个 `.passwd` 文件，里面包含 `sudo` 命令所需要使用的密码，例如：

```
!@#qweASD
<此处应有一个空行>
```

**注：密码后面需要跟随一个回车换行，详细说明请参考 `man sudo`。**

### 1.3、修改 mock server

打开 `~/benchmarker/mock/server.py` 修改 `do_POST` 方法中返回的数据，主要关注 `data` 字段，根据实际情况修改即可。

### 1.4、修改配置文件

1. 将 `~/benchmarker/workflow/bootstrap_samples.conf` 文件改名为 `bootstrap.conf`。
2. 将 `Host` 参数修改为 mock server 的地址，默认是 `http://localhost:3000`。
3. Token 参数可以随便取值。
4. RemoteHostUser 参数修改为被压机的登录用户（需要确保该用户能以免密码的形式 `ssh` 到被压机）。

## 二、运行

### 2.1、运行 mock server

```bash
$ cd ~/benchmarker/mock
$ ./server.py
```

### 2.2、运行压测脚本

```bash
$ cd ~/benchmarker/workflow
$ pipenv run python bootstrap.py -p <prefix>
```

**注：prefix 参数即为被压机的 hostname 前缀。**
