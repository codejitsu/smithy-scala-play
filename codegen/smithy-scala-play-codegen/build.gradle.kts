description = "Generates Scala Play Framework code from Smithy models"
extra["displayName"] = "Smithy :: ScalaPlayFramework :: Codegen"

val smithyVersion: String by project

buildscript {
    val smithyVersion: String by project

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("software.amazon.smithy:smithy-model:$smithyVersion")
    }
}

plugins {
    scala
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.scala.library)

    val smithyVersion: String by project

    // Smithy generic dependencies
    api("software.amazon.smithy:smithy-codegen-core:$smithyVersion")
    api("software.amazon.smithy:smithy-model:$smithyVersion")
    api("software.amazon.smithy:smithy-protocol-traits:$smithyVersion")
    api("software.amazon.smithy:smithy-protocol-test-traits:$smithyVersion")
    api("software.amazon.smithy:smithy-rules-engine:$smithyVersion")
    api("software.amazon.smithy:smithy-waiters:$smithyVersion")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}
