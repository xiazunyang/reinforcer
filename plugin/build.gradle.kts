plugins {
    `kotlin-dsl`
    id("maven-publish")
}

group = "com.github.xiazunyang"
version = "1.2.1"

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:4.2.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "cn.numeron"
            artifactId = "reinforcer"
            version = "1.2.1"
            from(components["java"])
        }
    }
}