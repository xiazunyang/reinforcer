package cn.numeron.reinforcer

open class Reinforcer {

    var outputPath: String? = null

    var installationPath: String? = null

    val renameMap = mutableMapOf<String, String>()

    fun rename(vararg entries: Pair<String, String>) {
        for (entry in entries) {
            renameMap[entry.first] = entry.second
        }
    }

}