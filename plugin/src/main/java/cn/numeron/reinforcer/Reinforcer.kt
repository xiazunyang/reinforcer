package cn.numeron.reinforcer

open class Reinforcer {

    /** 360加固保的账号 */
    var username: String? = null

    /** 360加固保的密码 */
    var password: String? = null

    /** 签名配置名称 */
    var signConfigName: String? = null

    /** 加固后apk的输出目录 */
    var outputDirectory: String? = null

    /** 360加固保的安装路径 */
    var installationPath: String? = null

    /** 加固后安装包的重命名映射 */
    val renameMap = mutableMapOf<String, String>()

    fun rename(vararg entries: Pair<String, String>) {
        for (entry in entries) {
            renameMap[entry.first] = entry.second
        }
    }

    fun rename(vararg keyOrValues: String) {
        keyOrValues.forEachIndexed { index, keyOrValue ->
            if (index % 2 == 0) {
                if (index < keyOrValues.lastIndex) {
                    renameMap[keyOrValue] = keyOrValues[index + 1]
                }
            }
        }
    }

}