### 360加固助手自动加固插件
当前最新版本号：[![](https://jitpack.io/v/cn.numeron/reinforcer.svg)](https://jitpack.io/#cn.numeron/reinforcer)

#### 介绍

* 基于360加固助手的自动化加固的Gradle插件，可以在AndroidStudio执行完assembleRelease相关的任务结束后自动使用360加固助手进行加固apk文件，并输出至指定的文件目录下。

* 支持多渠道打包。

#### 安装方法

1. 在android工程的根目录下build.gradle文件的适当位置添加以下代码：

```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'cn.numeron:reinforcer:latest_version'
    }
}
```

2. 在app模块的build.gradle文件的适当位置添加以下代码：

```groovy
apply plugin: 'com.android.application'
//添加以下代码
apply plugin: 'reinforcer-plugin'

//此行代码添加到android节点以下
reinforcer {
    //是否启用reinforcer
    enabled = true
    //360加固助手的登录账号与密码
    username = "账号"
    password = "密码"
    //如果android/buildTypes/release节点下已配置签名信息，则无需配置此参数
    //配置参数为signingConfigs节点下其中之一的名称
    signConfigName = "signing config name"
    //以下两项为必填项，否则无法运行加固功能
    outputDirection = "apk文件输出目录"
    installationPath = "360加固助手的jiagu.jar包完整路径"
}

```

#### 使用方法

* 执行打包任务即可，如：`gradlew app:assembleRelease`或`gradlew app:assembel[渠道名]Release`
* 等待任务执行结束后，到输入apk输出目录获取加固后的文件。
