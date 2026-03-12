plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "io.github.pzverkov.filesigner"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.annotation:annotation:1.9.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.benchmark:benchmark-junit4:1.3.3")
}

val groupId: String = project.findProperty("GROUP") as String
val artifactId: String = project.findProperty("POM_ARTIFACT_ID") as String
val versionName: String = project.findProperty("VERSION_NAME") as String

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                this.groupId = groupId
                this.artifactId = artifactId
                this.version = versionName

                pom {
                    name.set(project.findProperty("POM_NAME") as String)
                    description.set(project.findProperty("POM_DESCRIPTION") as String)
                    url.set(project.findProperty("POM_URL") as String)

                    licenses {
                        license {
                            name.set(project.findProperty("POM_LICENCE_NAME") as String)
                            url.set(project.findProperty("POM_LICENCE_URL") as String)
                        }
                    }

                    developers {
                        developer {
                            id.set(project.findProperty("POM_DEVELOPER_ID") as String)
                            name.set(project.findProperty("POM_DEVELOPER_NAME") as String)
                            url.set(project.findProperty("POM_DEVELOPER_URL") as String)
                        }
                    }

                    scm {
                        url.set(project.findProperty("POM_SCM_URL") as String)
                        connection.set(project.findProperty("POM_SCM_CONNECTION") as String)
                        developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as String)
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (versionName.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

                credentials {
                    username = System.getenv("OSSRH_USERNAME") ?: ""
                    password = System.getenv("OSSRH_PASSWORD") ?: ""
                }
            }
        }
    }

    signing {
        val signingKeyId = System.getenv("SIGNING_KEY_ID")
        val signingKey = System.getenv("SIGNING_KEY")
        val signingPassword = System.getenv("SIGNING_PASSWORD")
        if (signingKeyId != null && signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }
}
