pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral() // Maven Central 리포지토리
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") } // 카카오 리포지토리
    }
}

rootProject.name = "account"
include(":app")
 