import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    }
}
plugins {
    kotlin("jvm") version "2.0.10"
}

group = "io.dedyn.engineermantra"

repositories {
    //jcenter()
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

//val compile by configurations.creating

// In this section you declare the dependencies for your production and test code
dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "2.0.10")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.8.1")
    implementation(group = "org.json", name = "json", version = "20240303")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.5.6")
    //Due to bugs in the latest beta, we want to pull the latest commit from Jitpack instead of Maven.
    //implementation(group = "com.github.discord-jda", name = "JDA", version = "79b1b560b1")
    implementation(group = "net.dv8tion", name = "JDA", version = "5.0.1")
    implementation(group = "club.minnced", name = "discord-webhooks", version = "0.8.4")
    implementation(group = "org.mariadb.jdbc", name = "mariadb-java-client", version = "3.4.1")
    implementation(group = "net.java.dev.jna", name = "jna", version = "5.14.0")
    implementation(group = "net.java.dev.jna", name = "jna-platform", version = "5.14.0")
}

sourceSets {
    main {
        kotlin.srcDir("$projectDir/src/kotlin")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks {
    jar {
        manifest.attributes["Main-Class"] = "io.dedyn.engineermantra.omega.Main"
    }
    "build" {
        dependsOn(fatJar)
    }
}
val fatJar = task("fatJar", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.WARN
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = "1.0"
        attributes["Main-Class"] = "io.dedyn.engineermantra.omega.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}