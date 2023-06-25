import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.redreaperlp"
version = ""

repositories {
    mavenCentral()
}


dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.9")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20230227")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        manifest {
            attributes(
                    "Main-Class" to "com.github.redreaperlp.reaperutility.Main"
            )
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.github.redreaperlp.reaperutility.Main")
}