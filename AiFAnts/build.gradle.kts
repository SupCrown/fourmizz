plugins {
    java
    application
}

group = "com.aifants"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnitPlatform()
}

// Tâche pour exécuter l'application
tasks.register<JavaExec>("runAiFAnts") {
    group = "application"
    description = "Run AiFAnts simulation"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("main.java.Main")
}