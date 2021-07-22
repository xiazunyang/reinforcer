package cn.numeron.reinforcer

import org.gradle.api.Project

fun Project.getProperty(propertyName: String): String? {
    return if (hasProperty(propertyName)) property(propertyName) as? String else null
}