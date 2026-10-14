rootProject.name = "kdom"

pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral() }
    plugins { id("org.jetbrains.kotlin.multiplatform") version "2.4.21" }
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
