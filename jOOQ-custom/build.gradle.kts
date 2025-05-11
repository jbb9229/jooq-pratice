val jooqVersion = rootProject.extra["jooqVersion"] as String
val mysqlVersion = rootProject.extra["mysqlVersion"] as String

plugins {
    kotlin("jvm") version "1.9.25"
}

group = "com.boong"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq-codegen:${jooqVersion}")
    runtimeOnly("com.mysql:mysql-connector-j:${mysqlVersion}")
}