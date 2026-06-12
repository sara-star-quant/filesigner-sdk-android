pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "filesigner-sdk"
include(":filesigner-core")

val forcedVersions = mapOf(
    "org.bouncycastle:bcprov-jdk18on"                  to "1.84",
    "org.bouncycastle:bcpkix-jdk18on"                  to "1.84",
    "org.bouncycastle:bcutil-jdk18on"                  to "1.84",
    "io.netty:netty-buffer"                            to "4.1.133.Final",
    "io.netty:netty-codec"                             to "4.1.133.Final",
    "io.netty:netty-codec-http"                        to "4.1.133.Final",
    "io.netty:netty-codec-http2"                       to "4.1.133.Final",
    "io.netty:netty-codec-socks"                       to "4.1.133.Final",
    "io.netty:netty-common"                            to "4.1.133.Final",
    "io.netty:netty-handler"                           to "4.1.133.Final",
    "io.netty:netty-handler-proxy"                     to "4.1.133.Final",
    "io.netty:netty-resolver"                          to "4.1.133.Final",
    "io.netty:netty-transport"                         to "4.1.133.Final",
    "io.netty:netty-transport-native-unix-common"      to "4.1.133.Final",
    "org.bitbucket.b_c:jose4j"                         to "0.9.6",
    "org.jdom:jdom2"                                   to "2.0.6.1",
    "org.apache.commons:commons-lang3"                 to "3.18.0",
    "com.squareup.wire:wire-runtime"                   to "6.3.0",
    "com.squareup.wire:wire-runtime-jvm"               to "6.3.0",
    "org.apache.httpcomponents:httpclient"             to "4.5.14",
    "org.apache.httpcomponents:httpmime"               to "4.5.14",
)

gradle.beforeProject {
    val applyForce: ResolutionStrategy.() -> Unit = {
        eachDependency {
            forcedVersions["${requested.group}:${requested.name}"]?.let { useVersion(it) }
        }
    }
    buildscript.configurations.configureEach { resolutionStrategy(applyForce) }
    configurations.configureEach { resolutionStrategy(applyForce) }
}
