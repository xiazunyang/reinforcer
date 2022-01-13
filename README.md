### 360加固助手自动加固插件

当前最新版本号：[![](https://jitpack.io/v/cn.numeron/reinforcer.svg)](https://jitpack.io/#cn.numeron/reinforcer)

#### 介绍

* 基于`360加固宝`的自动化加固的`Gradle插件`，可以在`AndroidStudio`执行完`assembleRelease`相关的任务结束后自动使用`360加固宝`进行加固`apk`文件，并输出至指定的文件目录下。
* 支持`Windows`、`Mac OS`以及`Linux`系统。
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
    //执行完打包任务后是否加固，默认为是
    enabled = true
    //以下两项为必填项，否则无法运行加固功能
    outputDirectory = "apk文件输出目录"
    installationPath = "360加固助手的jiagu.jar包完整路径"
    //360加固助手的登录账号与密码，没有设置时，每次打包不进行登录操作，但是可能会加固失败。
    username = "账号"
    password = "密码"
    //默认情况下会读取android/buildTypes/release闭包下已配置的签名信息
    //配置参数为signingConfigs节点下其中之一的名称
    signConfigName = "signing config name"
    //默认情况下reinforcer加固后的文件名与项目中applicationVariants闭包下指定的文件名相同
    //可通过以下方式添加映射关系，当文件名(不包含扩展名)与key值匹配时，输出的文件名会修改为value的值
    rename += ["key1": "value1", "key2": "value2"]
}

```

#### 使用方法

* 启用`reinforcer`后，执行打包任务即可，如：`gradlew app:assembleRelease`或`gradlew app:assembel[渠道名]Release`
* 等待任务执行结束后，到输入apk输出目录获取加固后的文件。
* 执行`reinforce`任务可以单独执行加固任务，需要指定`inputApk`作为参数，参考以下3种方式：
   - 通过命令行执行任务时，需要添加`-PinputApi=[apk路径]`作为参数。
    ![](reinforce_task_0.png)
   - 通过`gradle`列表执行时，需要在`arguments`中填入`-PinputApi=[apk路径]`。
    ![](reinforce_task_1.png)
   - 或在`app`模块的`build.gradle`文件中添加以下代码指定要加固的`apk文件`后，直接运行`reinforce`任务即可。
     ```groovy
     tasks.getByName("reinforce") {
         inputApk = "build/outputs/apk/release/application.apk"
     }
     ```
#### 附录

* 指定项目打包时apk文件名的方法：
```groovy
android {
    applicationVariants.all { variant ->
        variant.outputs.all { output ->
            outputFileName = "打包后的apk包名.apk"
        }
    }
}
```

* 设置打包后自动签名的信息：
```groovy
android {
    signingConfigs {
        //release即此签名配置的名称
        release {
            keyAlias '...'
            keyPassword '...'
            storePassword '...'
            storeFile file("...")
        }
    }
    buildTypes {
        release {
            ...
            //指定打包时使用signingConfigs闭包下release的签名配置
            signingConfig signingConfigs.release
        }
    }
}
```
