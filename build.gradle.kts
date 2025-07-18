import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    }
}
plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.1.20"
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
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "2.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.10.2")
    implementation(group = "org.json", name = "json", version = "20240303")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.5.18")
    //Due to bugs in the latest beta, we want to pull the latest commit from Jitpack instead of Maven.
    //implementation(group = "com.github.discord-jda", name = "JDA", version = "79b1b560b1")
    implementation(group = "net.dv8tion", name = "JDA", version = "5.6.1")
    implementation(group = "club.minnced", name = "discord-webhooks", version = "0.8.4")
    //implementation(group = "org.mariadb.jdbc", name = "mariadb-java-client", version = "3.4.1")
    implementation(group = "net.java.dev.jna", name = "jna", version = "5.16.0")
    implementation(group = "net.java.dev.jna", name = "jna-platform", version = "5.16.0")
    //Instead of using Maria/MySQL, use a CassandraDB which is a NoSQL implementation.
    //This better represents the data we wish to store
    implementation("org.apache.cassandra:java-driver-core:4.19.0")
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
        manifest.attributes["Main-Class"] = "io.dedyn.engineermantra.botchelper.Main"
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
        attributes["Main-Class"] = "io.dedyn.engineermantra.botchelper.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}