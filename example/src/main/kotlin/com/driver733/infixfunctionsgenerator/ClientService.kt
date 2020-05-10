package com.driver733.infixfunctionsgenerator

@Suppress("UNUSED_PARAMETER")
class ClientService {

    @Infix("findByName")
    fun findBy(name: String) = "Alice"

    @Infix("deleteByName", "andAge")
    fun deleteBy(name: String, age: Int) = "Bob"

    @Infix("createWithName", "age", "andHeight")
    fun createBy(name: String, age: Int, height: Int) = "Carl"

}