plugins {
    `kotlin-dsl`
    id("com.github.dcendents.android-maven")
}

group = "com.github.xiazunyang"
version = "1.0.0"

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:4.2.2")
}