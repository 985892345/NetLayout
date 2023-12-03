plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  `maven-publish`
  signing
}

android {
  namespace = "com.ndhzs.netlayout"
  compileSdk = 34
  
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
  implementation("androidx.core:core-ktx:1.8.0")
  implementation("androidx.appcompat:appcompat:1.4.2")
}

group = "io.github.985892345"
version = "1.1.2-SNAPSHOT"
val projectArtifact = "NetLayout"
val projectGithubName = projectArtifact
val projectDescription = "一个功能更全面的网状布局"
val projectMainBranch = "master"

android {
  publishing {
    singleVariant("release") {
      withJavadocJar()
      withSourcesJar()
    }
  }
}

afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        groupId = project.group.toString()
        artifactId = projectArtifact
        version = project.version.toString()
        from(components["release"])
        signing {
          sign(this@create)
        }
        
        pom {
          name.set(projectArtifact)
          description.set(projectDescription)
          url.set("https://github.com/985892345/$projectGithubName")
          
          licenses {
            license {
              name.set("Apache-2.0 license")
              url.set("https://github.com/985892345/$projectGithubName/blob/$projectMainBranch/LICENSE")
            }
          }
          
          developers {
            developer {
              id.set("985892345")
              name.set("GuoXiangrui")
              email.set("guo985892345@formail.com")
            }
          }
          
          scm {
            connection.set("https://github.com/985892345/$projectGithubName.git")
            developerConnection.set("https://github.com/985892345/$projectGithubName.git")
            url.set("https://github.com/985892345/$projectGithubName")
          }
        }
      }
      repositories {
        maven {
          // https://s01.oss.sonatype.org/
          name = "mavenCentral" // 点击 publishReleasePublicationToMavenCentralRepository 发布到 mavenCentral
          val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
          val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
          setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
          credentials {
            username = project.properties["mavenCentralUsername"].toString()
            password = project.properties["mavenCentralPassword"].toString()
          }
        }
      }
    }
  }
}
