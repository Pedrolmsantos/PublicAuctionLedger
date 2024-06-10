plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.typesafe.akka:akka-actor_2.11:2.4.0")
    implementation("com.typesafe.akka:akka-testkit_2.11:2.4.0")
    implementation("junit:junit:4.11")
    implementation("io.netty:netty-all:5.0.0.Alpha1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
}

tasks.test {
    useJUnitPlatform()
}