import org.gradle.api.file.DuplicatesStrategy

plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.bible"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(24)  // 네 환경에 맞게 유지
}

application {
    // Main.kt 의 진입점 (패키지 기준
    mainClass.set("store.MainKt")
}
tasks.jar {
    // META-INF 충돌 방지
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // JAR에 Main-Class 기록 (java -jar 가능)
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // 런타임 의존성까지 전부 포함해서 하나의 JAR로 만든다 (fat/uber JAR)
    from(
        configurations.runtimeClasspath.get().map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
    )
}
