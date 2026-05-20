rootProject.name = "voxelmap"

pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}
rootProject.name = "VoxelMap - UHCTH"
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("common")
include("fabric")
include("forge")
include("neoforge")
