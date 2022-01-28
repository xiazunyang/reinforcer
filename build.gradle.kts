buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
    }
}