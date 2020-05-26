package com.driver733.infixfunctionsgenerator

import createWithName
import deleteByName
import findByName
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object ClientServiceTest : Spek({

    Feature("Infix functions generation") {
        val clients = ClientService()

        Scenario("fun with one param") {
            lateinit var actual: String
            When("infix fun is called") {
                actual = clients findByName "Alice"
            }
            Then("infix fun result is equal to the normal fun call") {
                assertThat(actual).isEqualTo(clients.findBy("Alice"))
            }
        }

        Scenario("fun with two params") {
            lateinit var actual: String
            When("infix fun is called") {
                actual = clients deleteByName "Bob" andAge 20
            }
            Then("infix fun result is equal to the normal fun call") {
                assertThat(actual).isEqualTo(clients.deleteBy("Bob", 20))
            }
        }

        Scenario("function with two params") {
            lateinit var actual: String
            When("infix fun is call") {
                actual = clients createWithName "Carl" age 25 andHeight 170
            }
            Then("infix fun result is equal to the normal fun call") {
                assertThat(actual).isEqualTo(clients.createBy("Carl", 25, 170))
            }
        }

    }

})
