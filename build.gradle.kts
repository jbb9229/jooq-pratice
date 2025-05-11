extra["jooqVersion"] = "3.19.22" // jOOQ 버전
val jooqVersion = extra["jooqVersion"] as String
extra["mysqlVersion"] = "8.3.0" // MySQL Connector/J 버전
val mysqlVersion = extra["mysqlVersion"] as String

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "9.0"
}

group = "com.boong"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin", "src/generated/jooq")
        }
        resources {
            srcDir("src/main/resources")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jooq") {
        exclude(group = "org.jooq", module = "jooq") // 플러그인에서 관리하는 jOOQ 버전과의 충돌 방지
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // MySQL 드라이버
    runtimeOnly("com.mysql:mysql-connector-j:${mysqlVersion}")

    implementation("org.jooq:jooq:${jooqVersion}")
    jooqGenerator("org.jooq:jooq:${jooqVersion}")
    jooqGenerator("org.jooq:jooq-meta:${jooqVersion}")
    jooqGenerator(project(":jOOQ-custom")) // 커스텀 생성기 전략
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// jOOQ 생성기를 위한 시스템 프로퍼티 (DB 접속 정보)
// Gradle 구성 단계에서 평가됩니다.
val dbUserProp: String = System.getProperty("db-user") ?: "root"
val dbPasswdProp: String = System.getProperty("db-passwd") ?: "passwd"

// jOOQ 플러그인 설정

jooq {
    version.set(jooqVersion)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS) // OSS 버전 사용

    configurations {
        create("sakilaDB") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "com.mysql.cj.jdbc.Driver" // MySQL 드라이버
                    url = "jdbc:mysql://localhost:3306" // DB URL
                    user = dbUserProp // DB 사용자 이름
                    password = dbPasswdProp // DB 비밀번호
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator" // 기본 생성기 사용
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "sakila" // 사용할 스키마 이름
//                        isUnsignedTypes = false // UIngeter와 같은 jOOQ 타입 사용 여부 Default: true
                    }
                    generate.apply {
                        isDaos = true // DAO 생성
                        isPojos = true // POJO 생성
                        isRecords = true // 레코드 생성
                        isFluentSetters = true // 플루언트 세터: pojo나 record 생성 시 return 값을 void 대신 객체 자체를 리턴
                        isJavaTimeTypes = true // java.time 패키지의 타입 사용
                        isDeprecated = false // deprecated된 메서드 사용 안 함

//                        그 외 커스텀 설정 들
//                        isJpaAnnotations = true // JPA 어노테이션 사용
//                        jpaVersion = "2.2" // JPA 버전
//                        isValidationAnnotations = true // 유효성 검사 어노테이션 사용
//                        isSpringAnnotations = true // Spring 어노테이션 사용
//                        isSpringDao = true // Spring DAO 어노테이션 사용
                    }
                    target.apply {
                        packageName = "com.boong.sakila.generated" // 생성된 코드의 패키지 이름
                        directory = "src/generated/jooq" // 생성된 코드의 디렉토리
                    }
                    strategy.name = "jooq.custom.generator.KPrefixGeneratorStrategy" // 커스텀 생성기 전략 사용
                }
            }
        }
    }
}