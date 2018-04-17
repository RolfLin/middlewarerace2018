class Task():

    def __init__(self, dict):
        self.dict = dict

    @property
    def task_id(self):
        return self.dict['taskid']

    @property
    def user_id(self):
        return self.dict['userid']

    @property
    def team_id(self):
        return self.dict['teamId']

    @property
    def team_code(self):
        return self.dict['teamCode']

    @property
    def code_path(self):
        return self.dict['gitpath']

    @property
    def image_path(self):
        return self.dict['imagepath']

    @property
    def docker_host(self):
        url = self.image_path
        return url.split('/')[0]

    @property
    def docker_username(self):
        return self.dict['imagerepouser']

    @property
    def docker_password(self):
        return self.dict['imagerepopassword']

    def __repr__(self):
        s = ', '.join(['{}={}'.format(k, v) for k, v in self.__dict__.items()])
        return '{}({})'.format(self.__class__.__name__, s)
