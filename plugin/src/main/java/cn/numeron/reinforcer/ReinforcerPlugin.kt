package cn.numeron.reinforcer

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class ReinforcerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create<Reinforcer>("reinforcer")
        project.tasks.create<ReinforceTask>("reinforce")
        project.tasks.whenTaskAdded {
            if (name.startsWith("assemble") && name.endsWith("Release")) {
                doLast {
                    reinforce(name, project)
                }
            }
        }
    }

    private fun reinforce(taskName: String, project: Project) {
        val reinforcer = project.extensions.getByType<Reinforcer>()
        if(!reinforcer.enabled) {
            project.logger.quiet("reinforcer plugin is disabled.")
            return
        }

        //取出app的gradle配置信息
        val appExtension = project.extensions.getByType<BaseAppModuleExtension>()
        //获取打包后apk的输出路径
        val variantName = toVariantName(taskName.removePrefix("assemble"))
        val variantOutput = appExtension.buildOutputs.findByName(variantName)
        if (variantOutput == null) {
            project.logger.quiet("variant $variantName not found. reinforce termination.")
            return
        }
        val outputFile = variantOutput.outputFile
        //找到加固的任务并执行
        val reinforceTask = project.tasks.getByName<ReinforceTask>("reinforce")
        reinforceTask.inputApk = outputFile
        reinforceTask.run()
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
