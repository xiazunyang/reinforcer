package cn.numeron.reinforcer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.concurrent.CountDownLatch

class ReinforcerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("reinforcer", Reinforcer::class.java)
        project.tasks.whenTaskAdded {
            if (name.startsWith("assemble") && name.endsWith("Release")) {
                doLast {
                    reinforce(project)
                }
            }
        }
    }

    private fun reinforce(project: Project) {
        //从运行时参数中获取指定的输出目录
        val outputPathForRuntime = project.getProperty("outputPath")

        //从运行时参数中获取指定的加固工具安装路径
        val installationPathForRuntime = project.getProperty("installationPath")

        val reinforcer = project.extensions.getByType<Reinforcer>()
        val renameMap = reinforcer.renameMap
        val outputPath = outputPathForRuntime ?: reinforcer.outputPath
                ?.takeIf {
                    it.isNotEmpty()
                }
        ?: throw NullPointerException("The output directory could not be found.")

        val installationPath = installationPathForRuntime ?: reinforcer.installationPath
                ?.takeIf {
                    it.isNotEmpty()
                }
        ?: throw NullPointerException("The installation path of the reinforcement tool could not be found.")

        project.logger.quiet("outputPath = [$outputPath], installationPath = [$installationPath], renameMap = [$renameMap]")

        //创建保存APK的文件夹
        val outputDir = File(outputPath)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val existFiles = outputDir.listFiles()?.map(File::getName) ?: emptyList()
        //获取CMD运行环境
        val runtime = Runtime.getRuntime()

        //获取输出目录下所有的发布版的apk文件
        File(project.buildDir, "outputs/apk")
                .listFiles()
                ?.mapNotNull {
                    //从release文件夹下找到最后修改的apk文件
                    File(it, "release")
                            .takeIf(File::exists)
                            ?.listFiles { _, name ->
                                name.endsWith(".apk")
                            }
                            ?.maxBy(File::lastModified)
                }?.let { apkFiles ->
                    val countDownLatch = CountDownLatch(apkFiles.size)
                    apkFiles.forEach { apkFile ->
                        Thread {
                            //加固
                            val command = "java -jar $installationPath -jiagu $apkFile $outputDir -autosign -automulpkg"
                            val process = runtime.exec(command)
                            project.logger.quiet("$apkFile reinforcing...")
                            process.waitFor()
                            project.logger.quiet("$outputDir reinforced!")
                            countDownLatch.countDown()
                        }.start()
                    }
                    countDownLatch.await()
                }
        //重命名加固后的文件
        outputDir.listFiles { _, name ->
            name.endsWith(".apk") && name !in existFiles
        }?.forEach { file ->
            project.logger.quiet("target file: $file")
            val outputFileName = file.name.substringBefore('_').let {
                renameMap[it] ?: it
            } + ".apk"
            val outputFile = File(outputDir, outputFileName)
            file.copyTo(outputFile, true)
            project.logger.quiet("renamed file: $outputFile")
            file.delete()
        }
    }

}
