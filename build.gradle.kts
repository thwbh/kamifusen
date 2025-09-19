plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.allopen") version "2.0.10"
    kotlin("plugin.noarg") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("io.quarkus")
    id("org.openapi.generator") version "7.8.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkiverse.quinoa:quarkus-quinoa:2.5.5")
    // quarkus & reactive JPA
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-security-jpa-reactive")
    implementation("io.quarkus:quarkus-jacoco")
    implementation("io.quarkus:quarkus-elytron-security-common")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")
    // OpenAPI
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-noarg")
    // ssr
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    // testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-test-vertx")
    testImplementation("io.quarkus:quarkus-test-security")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:kotlin-extensions:5.3.2")
}

group = "io.tohuwabohu"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        javaParameters.set(true)
    }
}

// OpenAPI Generator Configuration - use schema file
openApiGenerate {
    generatorName.set("typescript-axios")
    inputSpec.set("$projectDir/spec/openapi.yaml")
    outputDir.set("$projectDir/src/main/webui/src/api/gen")
    apiPackage.set("api")
    modelPackage.set("types")
    configOptions.set(mapOf(
        "supportsES6" to "true",
        "withSeparateModelsAndApi" to "true",
        "modelPropertyNaming" to "camelCase",
        "apiModulePrefix" to "Api",
        "npmName" to "",
        "npmVersion" to "",
        "snapshot" to "false",
        "withoutPrefixEnums" to "true"
    ))
    skipValidateSpec.set(true)
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)
}

openApiValidate {
    inputSpec.set("$projectDir/spec/openapi.yaml")
}
