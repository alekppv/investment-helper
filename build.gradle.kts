import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.0-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
}

group = "ru.dev-yandex"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://jitpack.io") }
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.github.microutils:kotlin-logging:2.1.23")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")

	runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("ru.tinkoff.piapi:java-sdk-core:1.19")
	implementation("org.ta4j:ta4j-core:0.16-SNAPSHOT")
	implementation("com.opencsv:opencsv:5.9")
	implementation("org.jfree:jfreechart:1.5.4")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
