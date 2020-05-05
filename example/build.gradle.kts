plugins {
    id("com.driver733.gradle-kotlin-setup-plugin")
}

group = "com.driver733.infix-functions-generator"

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":annotations"))
    implementation(project(":annotations"))
    kapt(project(":processor"))

    testImplementation( "com.github.driver733:assertk-core:deps~update-libs-SNAPSHOT")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.9")
    testRuntimeOnly("org.spekframework.spek2:spek-runtime-jvm:2.0.9")
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
        }
    }
}
