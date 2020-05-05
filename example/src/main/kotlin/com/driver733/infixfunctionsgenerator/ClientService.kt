package com.driver733.infixfunctionsgenerator

@Suppress("UNUSED_PARAMETER")
class ClientService {

    @GenerateInfix("findByName")
    fun findBy(name: String) = "Alice"

    @GenerateInfix("deleteByName", "andAge")
    fun deleteBy(name: String, age: Int) = "Bob"

    @GenerateInfix("createWithName", "age", "andHeight")
    fun createBy(name: String, age: Int, height: Int) = "Carl"

}