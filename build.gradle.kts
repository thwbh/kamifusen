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

// Add generated sources to source sets
sourceSets {
    main {
        java {
            srcDir("build/generated/server-api")
        }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateServerApi")

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        javaParameters.set(true)
    }
}

// Server API generation (Kotlin with custom templates)
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateServerApi") {
    generatorName.set("kotlin-server")
    inputSpec.set("$projectDir/spec/openapi.yaml")
    outputDir.set("$projectDir/build/generated/server-api")
    templateDir.set("$projectDir/openapi-templates/kotlin-server")
    apiPackage.set("io.tohuwabohu.kamifusen.api.generated")
    modelPackage.set("io.tohuwabohu.kamifusen.api.generated.model")

    configOptions.set(mapOf(
        "library" to "jaxrs-spec",
        "interfaceOnly" to "true",
        "returnResponse" to "true",
        "serializationLibrary" to "jackson",
        "enumPropertyNaming" to "UPPERCASE",
    ))

    typeMappings.set(mapOf(
        "LocalDateTime" to "java.time.LocalDateTime",
        "DateTime" to "java.time.LocalDateTime",
        "BigDecimal" to "kotlin.Double"
    ))

    additionalProperties.set(mapOf(
        "reactive" to "true",
        "mutiny" to "true"
    ))
}

// Client API generation (TypeScript)
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateClientApi") {
    generatorName.set("typescript-axios")
    inputSpec.set("$projectDir/spec/openapi.yaml")
    outputDir.set("$projectDir/src/main/webui/src/api")
    apiPackage.set("api")
    modelPackage.set("types")
    configOptions.set(
        mapOf(
            "supportsES6" to "true",
            "withSeparateModelsAndApi" to "true",
            "modelPropertyNaming" to "camelCase",
            "apiModulePrefix" to "Api",
            "npmName" to "",
            "npmVersion" to "",
            "snapshot" to "false",
            "withoutPrefixEnums" to "true",
            "typescriptThreePlus" to "true",
            "stringEnums" to "true"
        )
    )
    skipValidateSpec.set(true)
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)

    doLast {
        delete(fileTree("$projectDir/src/main/webui/src/api") {
            exclude("*.ts")
            exclude("**/*.ts")
        })
    }
}

tasks.register("generateApi") {
    dependsOn("generateServerApi")
    dependsOn("generateClientApi")
}

// Generate .env file for React app with current version
tasks.register("generateReactEnv") {
    doFirst {
        val appVersion = System.getProperty("quarkus.application.version") ?: project.version.toString()
        val envFile = file("$projectDir/src/main/webui/.env")
        envFile.parentFile.mkdirs()
        envFile.writeText("VITE_APP_VERSION=$appVersion\n")
        println("Generated .env file at: ${envFile.absolutePath}")
        println("Generated .env file with version: $appVersion")
    }
}

// Run the .env generation at the very start of any build
gradle.projectsEvaluated {
    tasks.named("generateReactEnv").get().let { envTask ->
        tasks.all {
            if (name.startsWith("quarkus") || name.contains("Build")) {
                dependsOn(envTask)
            }
        }
    }
}