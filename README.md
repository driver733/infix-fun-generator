# Infix functions generator

[![Build](https://github.com/driver733/infix-functions-generator/workflows/Build/badge.svg?branch=master)](https://github.com/driver733/infix-functions-generator/actions?query=workflow%3ABuild+branch%3Amaster)
[![Maven Central](https://img.shields.io/maven-central/v/com.driver733.infix-fun-generator/annotations)](https://search.maven.org/search?q=com.driver733.infix-fun-generator)

[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/driver733/infix-functions-generator/actions?query=workflow%3ARelease)

[![Licence](https://img.shields.io/github/license/driver733/infix-functions-generator)](https://github.com/driver733/infix-functions-generator/blob/master/LICENSE)

This project allows generating [infix](https://kotlinlang.org/docs/reference/functions.html#infix-notation)
[extension functions](https://kotlinlang.org/docs/reference/extensions.html#extension-functions)
for instance methods with multiple parameters.

For example, by annotating the `createBy` instance method with the `@Infix` annotation,

```kotlin
class ClientService {
    // intermediate methods' names are customizable (default = "and")
    @Infix("createWithName", "age", "andHeight") 
    fun createBy(name: String, age: Int, height: Int) = "Carl"
}
```

the `ClientService` can be used in a DSL fashion:

```kotlin
clientService createWithName "Alex" age 23 andHeight 170
```

Such notation adopts the codebase to the DDD, allowing reading the code as sentences in a context of the domain.

## Distribution

This project is [available](https://search.maven.org/search?q=com.driver733.infix-fun-generator) on the Maven Central repository.

## Getting Started

### Install

#### Gradle

##### Groovy DSL

Add this to your project's `build.gradle`:

```groovy
dependencies {
    implementation 'com.driver733.infix-functions-generator:annotation:2.0.1'
    annotationProcessor 'com.driver733.infix-functions-generator:processor:2.0.1'
}
```

##### Kotlin DSL

1. Apply the [`KAPT` plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.kapt).

    ```kotlin
    plugins {
      id("org.jetbrains.kotlin.kapt") version "1.3.72"
    }
    ```
2. Add this to your project's `build.gradle.kts`:

    ```kotlin
    dependencies {
        implementation("com.driver733.infix-functions-generator:annotation:2.0.1")
        kapt("com.driver733.infix-functions-generator:processor:2.0.1")
    }
    ``` 

#### Maven

Add this to your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
      <groupId>com.driver733.infix-functions-generator</groupId>
      <artifactId>annotations</artifactId>
      <version>2.0.1</version>
    </dependency>
    <dependency>
      <groupId>com.driver733.infix-functions-generator</groupId>
      <artifactId>processor</artifactId>
      <version>2.0.1</version>
      <scope>provided</scope>
    </dependency>
</dependencies>
```

## Development

### Prerequisites

[JDK](https://stackoverflow.com/a/52524114/2441104), preferably >= `v. 1.8`

### Build

```
./gradlew clean build
```

### CI/CD

[Github actions](https://github.com/driver733/infix-functions-generator/actions) is used for CI/CD.

### Releases

Releases to the Maven Central repository are [automatically](https://github.com/driver733/infix-functions-generator/actions?query=workflow%3ARelease)
made on each commit on the master branch with the help of the [semantic-release](https://github.com/semantic-release/semantic-release).

## Contributing

1. Create an issue and describe your problem/suggestion in it.
2. Submit a pull request with a reference to the issue that the pull request closes.
3. I will review your changes and merge them.
4. A new version with your changes will be released automatically right after merging.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags](https://github.com/driver733/infix-functions-generator/tags). 

## Authors

* **Mikhail [@driver733](https://www.driver733.com) Yakushin** - *Initial work*

See also the list of [contributors](https://github.com/driver733/infix-functions-generator/graphs/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/driver733/infix-functions-generator/blob/master/LICENSE) file for details.

## Acknowledgments

* [semantic-release](https://github.com/semantic-release/semantic-release)
