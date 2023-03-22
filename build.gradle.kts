buildscript {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.4.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
  }
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}