plugins {
    id("java")
    id("io.freefair.lombok") version "6.5.0-rc1"
    id("io.papermc.paperweight.userdev") version "1.3.7"
}

repositories {
    mavenCentral()
    maven { url = uri("https://sonatype.projecteden.gg/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://maven.citizensnpcs.co/repo") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.onarandombox.com/content/groups/public/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://mvnrepository.com/artifact/org.apache.commons/commons-collections4") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/") }
    maven {
        url = uri("https://repo.inventivetalent.org/content/groups/public/")
        content { includeGroup("org.inventivetalent") }
    }
}

dependencies {
    paperweightDevBundle("gg.projecteden.parchment", "1.19.4-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.8-SNAPSHOT")
    implementation("dev.morphia.morphia:core:1.6.2-SNAPSHOT")
    implementation("dev.dbassett:skullcreator:3.0.1")
    implementation("com.squareup.okhttp3:okhttp:3.14.6")
    implementation("org.objenesis:objenesis:3.2")
    compileOnly("gg.projecteden.parchment:parchment-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.6-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.31-SNAPSHOT") {
        exclude("*", "*")
    }
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.10.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.1")
    compileOnly(files("libs/GlowAPI_v1.5.7-SNAPSHOT.jar"))
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