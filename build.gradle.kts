import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot")           version "3.2.4"
    id("io.spring.dependency-management")    version "1.1.4"
}

group   = "com.hospital"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val jjwtVersion = "0.12.5"

repositories {
    mavenCentral()
}

// ─── Global logging exclusions ───────────────────────────────────────────────
// Every Spring Boot starter (data-jpa, security, validation, etc.) transitively
// pulls in spring-boot-starter-logging (Logback) and log4j-to-slf4j.
// We exclude them globally here so Log4j2 is the sole logging provider.
// Without this, log4j-slf4j2-impl and log4j-to-slf4j both end up on the
// classpath and cause: "log4j-slf4j2-impl cannot be present with log4j-to-slf4j"
configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "ch.qos.logback",           module = "logback-classic")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {

    // Spring Boot Web (logging exclusion handled globally above)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Log4j2 — sole logging implementation
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

tasks.named<BootJar>("bootJar") {
    archiveFileName = "hospital-management-system.jar"
    manifest {
        attributes["Implementation-Title"]   = "Hospital Management System"
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}