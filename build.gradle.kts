plugins {
	java
	application
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(fileTree("jars") {
		include("*.jar")
	})

	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events("passed", "skipped", "failed")
	}
}

application {
	mainClass.set("TUIDemo")
}