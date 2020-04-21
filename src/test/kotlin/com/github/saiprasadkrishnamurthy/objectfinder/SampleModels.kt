package com.github.saiprasadkrishnamurthy.objectfinder

/*
    Sample models for testing.
 */
data class Developer(val name: String, val language: String, val framework: String)

val developers = listOf(
        Developer("Sai", "Kotlin", "SpringBoot"),
        Developer("Kris", "Kotlin", "JavaEE"),
        Developer("Joe", "Kotlin", "SpringBoot"),
        Developer("Jane", "Java", "SpringBoot"),
        Developer("Sanga", "Scala", "Play")
)