package cn.numeron.reinforcer

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.io.File

class ReinforcerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create<Reinforcer>("reinforcer")
        project.tasks.whenTaskAdded {
            if (name.startsWith("assemble") && name.endsWith("Release")) {
                dependsOn(":clean")
                doLast {
                    reinforce(name, project)
                }
            }
        }
    }

    private fun reinforce(taskName: String, project: Project) {
        val reinforcer = project.extensions.getByType<Reinforcer>()
        val renameMap = reinforcer.renameMap

        val installationPath = reinforcer.installationPath
            ?: throw NullPointerException("The installation path of the reinforcement tool could not be found.")

        val outputPath = reinforcer.outputDirectory
            ?: throw NullPointerException("The output directory could not be found.")

        val username = reinforcer.username
        val password = reinforcer.password

        //登录
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            project.exec {
                executable("java")
                args("-jar", installationPath, "-login", username, password)
            }.rethrowFailure()
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
                executable("java")
                args("-jar", installationPath, "-importsign", storeFile, storePassword, keyAlias, keyPassword)
            }.rethrowFailure()
        }

        //获取打包后apk的输出路径
        val variantName = toVariantName(taskName.removePrefix("assemble"))
        val variantOutput = appExtension.buildOutputs.findByName(variantName)
        if (variantOutput == null) {
            project.logger.quiet("variant $variantName not found. reinforce termination.")
            return
        }
        val outputFile = variantOutput.outputFile

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
            executable("java")
            args("-jar", installationPath, "-jiagu", outputFile, outputDirectory, "-autosign", "-automulpkg")
        }.rethrowFailure()

        //获取新加固的文件
        val reinforcedApkFile = outputDirectory.listFiles { _, name ->
            name.endsWith(".apk") && name !in existFiles
        }?.firstOrNull()

        //重命名加固后的文件
        if (reinforcedApkFile != null) {
            project.logger.quiet("target file: $reinforcedApkFile")
            val outputFileName = reinforcedApkFile.name.substringBefore('_').let {
                renameMap[it] ?: it
            } + ".apk"
            val outputApkFile = File(outputDirectory, outputFileName)
            reinforcedApkFile.copyTo(outputApkFile, true)
            project.logger.quiet("renamed file: $outputApkFile")
            reinforcedApkFile.delete()
        }

    }

    /** 从任务名称中取出变体名称 */
    private fun toVariantName(taskName: String): String {
        val stringBuilder = StringBuilder()
        taskName.forEach {
            if (it.isUpperCase()) {
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append("-")
                }
                stringBuilder.append(it.toLowerCase())
            } else {
                stringBuilder.append(it)
            }
        }
        return stringBuilder.toString()
    }


}
