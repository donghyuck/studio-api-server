plugins {
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
    war
    java
}

group = "com.podosoftware"
version = "0.0.1-SNAPSHOT"

val profile = project.findProperty("profile") as String? ?: "dev"
val isDev = profile == "dev"

logger.lifecycle("📦 [PROFILE] = $profile (isDev=$isDev)")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11)) 
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}
repositories {
    mavenCentral()
    maven { 
        name="nexus" 
        url = uri("http://localhost:8081/repository/maven-public") 
        isAllowInsecureProtocol = true
    }
}

dependencies {
    if (isDev) {
        implementation("org.springframework.boot:spring-boot-starter-tomcat")
    } else {
        providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    }

    // platform starters
    implementation("studio.api:studio-platform-starter:0.0.2")
    implementation("studio.api:studio-platform-starter-jasypt:0.0.2")
    implementation("studio.api:studio-platform-starter-user:0.0.2")
    implementation("studio.api:studio-platform-starter-security:0.0.2")

    // srping starters
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-crypto")

    // database driver
    implementation("org.postgresql:postgresql:42.7.7")    
    implementation("org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16")
    implementation("org.flywaydb:flyway-core")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    //lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
	testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    //mapstruct 
    implementation ("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor ("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor ("org.projectlombok:lombok-mapstruct-binding:0.2.0")    

}
tasks.test { useJUnitPlatform() }
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = isDev
}
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    enabled = !isDev
}
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
     jvmArgs = listOf("-Dspring.profiles.active=${profile}")
     if (isDev) {
        systemProperty("JASYPT_ENCRYPTOR_PASSWORD", project.findProperty("JASYPT_ENCRYPTOR_PASSWORD") )
    }
}