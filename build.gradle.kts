plugins {
    id("java")
    id("io.freefair.lombok") version "6.5.0-rc1"
    id("io.papermc.paperweight.userdev") version "1.3.7"
}

repositories {
    mavenCentral()
    maven { url = uri("https://sonatype.projecteden.gg/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
}

dependencies {
    paperweightDevBundle("gg.projecteden.parchment", "1.19.4-R0.1-SNAPSHOT")
    compileOnly("gg.projecteden.parchment:parchment-api:1.19.4-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.8-SNAPSHOT")
}

group = "net"
version = "1.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        options.compilerArgs.add("-parameters")
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}