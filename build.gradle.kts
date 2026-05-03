buildscript {
    configurations.classpath {
        resolutionStrategy.eachDependency {
            when ("${requested.group}:${requested.name}") {
                "org.bouncycastle:bcprov-jdk18on"      -> useVersion("1.84")
                "org.bouncycastle:bcpkix-jdk18on"      -> useVersion("1.84")
                "io.netty:netty-codec-http2"           -> useVersion("4.1.132.Final")
                "io.netty:netty-codec-http"            -> useVersion("4.1.132.Final")
                "io.netty:netty-codec"                 -> useVersion("4.1.125.Final")
                "io.netty:netty-common"                -> useVersion("4.1.118.Final")
                "io.netty:netty-handler"               -> useVersion("4.1.118.Final")
                "org.bitbucket.b_c:jose4j"             -> useVersion("0.9.6")
                "org.jdom:jdom2"                       -> useVersion("2.0.6.1")
                "org.apache.commons:commons-lang3"     -> useVersion("3.18.0")
                "com.squareup.wire:wire-runtime"       -> useVersion("5.2.0")
            }
        }
    }
}

plugins {
    id("com.android.library") version "9.1.0" apply false
}
