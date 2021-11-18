buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/jcenter")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    }
}