plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("io.github.985892345.MavenPublisher") version "1.1.1"
}

android {
  namespace = "com.ndhzs.netlayout"
  compileSdk = 35
  
  defaultConfig {
    minSdk = 21

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }
  
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.appcompat:appcompat:1.7.0")
}

group = "io.github.985892345"
version = "1.1.2"

publisher {
  masterDeveloper = DeveloperInformation(
    githubName = "985892345",
    email = "guo985892345@formail.com"
  )
  description = "一个功能更全面的网状布局"
  artifactId = "NetLayout"
  githubRepositoryName = "NetLayout"
  mainBranch = "master"
}
