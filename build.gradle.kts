plugins { id("org.jetbrains.kotlin.multiplatform") }

group = "fi.sykero"
version = "0.1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    jvm()
    iosArm64()
    iosSimulatorArm64()
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    sourceSets {
        commonMain.dependencies {
            implementation("io.github.pdvrieze.xmlutil:core:1.0.2.1")
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmTest.dependencies {
            implementation("junit:junit:4.13.2")
            implementation("org.jdom:jdom2:2.0.6.1")   // oracle only
            implementation("jaxen:jaxen:2.0.0")        // oracle only
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("kdom.corpus", System.getProperty("kdom.corpus") ?: "")
}

// iOS simulator tests: stream stdout/stderr so a test process that dies mid-test (SIGSEGV) still leaves its last
// output in the CI log (mirrors :jsword).
tasks.withType<AbstractTestTask>().matching { it.name == "iosSimulatorArm64Test" }.configureEach {
    testLogging {
        showStandardStreams = true
        events("started", "passed", "failed", "skipped")
    }
}
