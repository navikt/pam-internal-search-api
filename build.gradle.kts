plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    application
}

group = "no.nav.arbeid.internal-search-api"
version = "0.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

val javalinVersion = "7.2.3"
val micrometerVersion = "1.17.1"
val opensearchVersion = "3.9.0"
val logbackEncoderVersion = "9.0"
val junitVersion = "5.14.1"
val assertjVersion = "3.27.7"
val testcontainersVersion = "2.0.5"
val opensearchTestcontainersVersion = "4.1.0"

dependencies {
    implementation(platform("io.javalin:javalin-bom:$javalinVersion"))
    implementation("io.javalin:javalin-bundle")
    implementation("io.javalin:javalin-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("org.opensearch.client:opensearch-java:$opensearchVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("io.javalin:javalin-testtools:$javalinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.opensearch:opensearch-testcontainers:$opensearchTestcontainersVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "no.nav.arbeid.search.api.ApplicationKt"
}

tasks.test {
    useJUnitPlatform()
}
