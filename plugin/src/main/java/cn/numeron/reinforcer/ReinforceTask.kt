package cn.numeron.reinforcer

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.io.File

open class ReinforceTask : DefaultTask() {

    private var apkPath: String? = null

    @Internal
    var inputApk: Any? = null
        set(value) {
            apkPath = when (value) {
                is String -> value
                is File -> value.absolutePath
                else -> throw IllegalArgumentException("inputApk must be a file or file path.")
            }
            field = value
        }

    @TaskAction
    fun run() {
        //取出要加固的APK路径参数
        val inputApkPath = if (project.hasProperty(INPUT_APK_PATH)) {
            project.property(INPUT_APK_PATH) as String
        } else if (apkPath != null) {
            apkPath as String
        } else {
            return logger.quiet("reinforce task terminated, not found input apk.")
        }

        //取出加固的配置信息
        val reinforcer = project.extensions.getByType<Reinforcer>()

        //jiagu.jar的路径
        val jiaguPath = reinforcer.installationPath
            ?: throw NullPointerException("The installation path of the reinforcement tool could not be found.")

        val outputPath = reinforcer.outputDirectory
            ?: throw NullPointerException("The output directory could not be found.")

        val username = reinforcer.username
        val password = reinforcer.password

        //加固包的安装路径
        val installationPath = reinforcer.installationPath?.substringBeforeLast(File.separator)
        //加固包内置的java路径
        val javaPath = installationPath + File.separator + "java${File.separatorChar}bin${File.separatorChar}java"

        //登录
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            project.exec {
                setWorkingDir(installationPath)
                executable(javaPath)
                args("-jar", jiaguPath, "-login", username, password)
            }.rethrowFailure().assertNormalExitValue()
        }

        val appExtension = project.extensions.getByType<BaseAppModuleExtension>()

        //导入签名信息
        val signConfigName = reinforcer.signConfigName

        val buildType = appExtension.buildTypes["release"]
        var signingConfig = buildType.signingConfig

        if (signingConfig == null && !signConfigName.isNullOrEmpty()) {
            //如果编译类别中没有签名配置，则通过指定的名称查找
            signingConfig = appExtension.signingConfigs.findByName(signConfigName)
        }

        if (signingConfig != null) {
            val storePassword = signingConfig.storePassword
            val keyPassword = signingConfig.keyPassword
            val storeFile = signingConfig.storeFile
            val keyAlias = signingConfig.keyAlias

            //设置签名信息
            project.exec {
                setWorkingDir(installationPath)
                executable(javaPath)
                args("-jar", jiaguPath, "-importsign", storeFile, storePassword, keyAlias, keyPassword)
            }.rethrowFailure().assertNormalExitValue()
        }

        //创建保存APK的文件夹
        val outputDirectory = File(outputPath)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        //获取已存在的apk文件列表
        val existFiles = outputDirectory.list { _, name ->
            name.endsWith(".apk")
        } ?: emptyArray()

        //执行打包脚本
        project.exec {
            setWorkingDir(installationPath)
            executable(javaPath)
            args("-jar", jiaguPath, "-jiagu", inputApkPath, outputDirectory, "-autosign", "-automulpkg")
        }.rethrowFailure().assertNormalExitValue()

        //获取新加固的文件
        val reinforcedApkFile = outputDirectory.listFiles { _, name ->
            name.endsWith(".apk") && name !in existFiles
        }?.firstOrNull()
        project.logger.quiet("reinforced apk file: $reinforcedApkFile")

        //重命名加固后的文件
        if (reinforcedApkFile != null) {
            val filename = inputApkPath.substringBeforeLast('.').substringAfterLast(File.separatorChar)
            val outputFileName = reinforcer.rename[filename] ?: filename
            val outputApkFile = File(outputDirectory, "$outputFileName.apk")
            reinforcedApkFile.copyTo(outputApkFile, true)
            project.logger.quiet("renamed apk file: $outputApkFile")
            reinforcedApkFile.delete()
        }

    }

    private companion object {
        private const val INPUT_APK_PATH = "inputApk"
    }

}