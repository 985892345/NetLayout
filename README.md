# NetLayout
 一个功能更全面的网状布局

在之前接到了写掌邮新课表的需求，因为代码过老，有了重构的想法，所以写了一个网状布局，课表项目请查看
 [CourseViewLibrary](https://github.com/985892345/CourseViewLibrary) ，本项目是支撑整个课表的基础控件

## 添加依赖
目前先使用 jitpack 依赖，后面有时间了再去弄 mavenCentral

版本号：[![](https://jitpack.io/v/985892345/NetLayout.svg)](https://jitpack.io/#985892345/NetLayout)
````kotlin
// settings.gradle.kts
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven("https://jitpack.io")
  }
}

// build.gradle.kts
dependencies {
 implementation("com.github.985892345:NetLayout:xxx") // 版本号请看上方的 jitpack 标签
}
````

目前还没有发布正式包，如果想提前体验，可以试试快照版本
````kotlin

// 设置 gradle 不缓存依赖
configurations.all {
 resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

dependencies {
 implementation("com.github.985892345:NetLayout:master-SNAPSHOT")
}
````