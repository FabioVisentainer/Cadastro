plugins {
	java
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.teste"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	/* ───────── Spring ───────── */
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	/* Banco */
	runtimeOnly("com.mysql:mysql-connector-j")

	/* Testes */
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	/* Bootstrap */
	implementation("org.webjars:bootstrap:5.3.3")
	implementation("org.webjars.npm:bootstrap-icons:1.11.3")
}

tasks.withType<Test> {
	useJUnitPlatform()
}