plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "pt.isel.daw"
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

	// for Spring validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// To get the DI annotation
	implementation("jakarta.inject:jakarta.inject-api:2.0.1")

	// for JDBI
	implementation("org.jdbi:jdbi3-core:3.37.1")
	implementation("org.jdbi:jdbi3-kotlin:3.37.1")
	implementation("org.jdbi:jdbi3-postgres:3.37.1")
	implementation("org.postgresql:postgresql:42.7.2")

	// To use Kotlin specific date and time functions
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

	// To get password encode
	implementation("org.springframework.security:spring-security-core:6.3.0")

	// To use WebTestClient on tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
	if (System.getenv("DB_URL") == null) {
		environment("DB_URL", "jdbc:postgresql://localhost:5432/DAW_tests?user=postgres&password=12345")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}


kotlin {
	jvmToolchain(21)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task<Copy>("extractUberJar") {
	dependsOn("assemble")
	// opens the JAR containing everything...
	from(zipTree("$buildDir/libs/daw-$version.jar"))
	// ... into the 'build/dependency' folder
	into("build/dependency")
}

task<Exec>("composeUp") {
	commandLine("docker-compose", "up", "--build", "--force-recreate")
	dependsOn("extractUberJar")
}

task<Exec>("composeStart") {
	commandLine("docker-compose", "up", "-d")
	dependsOn("extractUberJar")
}

task<Exec>("composeDown") {
	commandLine("docker-compose", "down")
}

task<Exec>("composeStop") {
	commandLine("docker-compose", "stop")
}
