plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "pt.isel.daw.channels"
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
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.jdbi:jdbi3-core:3.37.1")
	implementation("org.jdbi:jdbi3-kotlin:3.37.1")
	implementation("org.jdbi:jdbi3-postgres:3.37.1")
	implementation("org.postgresql:postgresql:42.5.4")
}

tasks.test {
	useJUnitPlatform()
	if (System.getenv("DB_URL") == null) {
		environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=postgres&password=postgres")
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