import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `kotlin-dsl`
    id("com.vanniktech.maven.publish")
}

group = "cn.numeron"
version = "1.0.1"

dependencies {
    implementation(gradleApi())
}

mavenPublish {
    sonatypeHost = SonatypeHost.S01
}