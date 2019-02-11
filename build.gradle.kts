import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.20")
    id("kotlinx-serialization").version("1.3.20")
    id("com.github.johnrengelman.shadow").version("4.0.4")
    id("org.jlleitschuh.gradle.ktlint").version("7.0.0")
}

repositories {
    jcenter()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.10.0")
    implementation("com.github.ajalt:clikt:1.6.0")
    implementation("org.apache.commons:commons-csv:1.6")
    implementation("me.tongfei:progressbar:0.7.2")

    implementation("org.jooq:jooq:3.11.9")
    implementation("org.postgresql:postgresql:42.2.5.jre7")

    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation("org.tinylog:tinylog:1.3.5")
    implementation("org.tinylog:slf4j-binding:1.3.5")

    testImplementation("io.strikt:strikt-core:0.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

val shadowJar by tasks.getting(ShadowJar::class) {
    manifest.attributes["Main-Class"] = "de.hpi.swa.testprio.AppKt"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
