# 开发环境搭建（仅限 macOS）

## 安装 [Homebrew](https://brew.sh/)

```bash
$ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

## 安装 Python 3

```bash
$ brew install python
```

**注：新版的 Homebrew 运行 `brew install python` 命令会默认安装 Python 3。**

## 安装 [Pipenv](https://docs.pipenv.org/)

```bash
$ brew install pipenv
```

## 创建 Python 执行环境

```bash
$ cd ./workflow
$ pipenv install
```

## 运行

### 运行 Python 脚本

```bash
$ pipenv run python bootstrap.py -p <prefix>
```

### 运行 Shell 脚本

需要本机安装 ossutil 和 wrk。

```bash
$ cd ./scripts
$ sh bootstrap.sh <prefix>
```
