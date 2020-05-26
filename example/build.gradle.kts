plugins {
    id("com.driver733.gradle-kotlin-setup-plugin")
}

group = "com.driver733.infix-functions-generator"

dependencies {
    implementation(project(":annotations"))
    implementation(project(":annotations"))
    kapt(project(":processor"))

    testCompile("org.assertj:assertj-core:3.11.1")
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
