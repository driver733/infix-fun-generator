package com.driver733.infixfunctionsgenerator

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Infix(val firstMethodName: String = "", vararg val intermediateMethodsNames: String = [])