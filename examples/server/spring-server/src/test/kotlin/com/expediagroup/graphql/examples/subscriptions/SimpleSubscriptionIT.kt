/*
 * Copyright 2019 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.expediagroup.graphql.examples.subscriptions

import com.expediagroup.graphql.examples.SUBSCRIPTION_ENDPOINT
import com.expediagroup.graphql.server.spring.subscriptions.SubscriptionOperationMessage
import com.expediagroup.graphql.server.spring.subscriptions.SubscriptionOperationMessage.ClientMessages.GQL_CONNECTION_INIT
import com.expediagroup.graphql.server.spring.subscriptions.SubscriptionOperationMessage.ClientMessages.GQL_START
import com.expediagroup.graphql.server.spring.subscriptions.SubscriptionOperationMessage.ServerMessages
import com.expediagroup.graphql.types.GraphQLRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.FixMethodOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runners.MethodSorters
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.publisher.TestPublisher
import java.net.URI
import kotlin.random.Random

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["graphql.packages=com.expediagroup.graphql.examples"]
)
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SimpleSubscriptionIT(@LocalServerPort private var port: Int) {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `verify singleValueSubscription query`() {
        val query = "singleValueSubscription"
        val subscription = subscribe(query)

        StepVerifier.create(subscription)
            .expectNext("{\"data\":{\"$query\":1}}")
            .expectComplete()
            .verify()
    }

    @Test
    fun `verify counter query`() {
        val query = "counter(limit: 2)"
        val numberRegex = "(\\-?[0-9]+)"
        val expectedDataRegex = Regex("\\{\"data\":\\{\"counter\":$numberRegex}}")
        val subscription = subscribe(query)

        StepVerifier.create(subscription)
            .expectNextMatches { s -> s.matches(expectedDataRegex) }
            .expectNextMatches { s -> s.matches(expectedDataRegex) }
            .expectComplete()
            .verify()
    }

    @Test
    fun `verify singleValueThenError query`() {
        val query = "singleValueThenError"
        val subscription = subscribe(query)

        StepVerifier.create(subscription)
            .expectNextCount(1)
            .expectComplete()
            .verify()
    }

    @Test
    fun `verify flow query`() {
        val query = "flow"
        val subscription = subscribe(query)

        StepVerifier.create(subscription)
            .expectNext("{\"data\":{\"$query\":1}}")
            .expectNext("{\"data\":{\"$query\":2}}")
            .expectNext("{\"data\":{\"$query\":4}}")
            .expectComplete()
            .verify()
    }

    @Test
    fun `verify subscriptionContext query without connectionParams`() {
        val query = "subscriptionContext"
        val subscription = subscribe(query)

        StepVerifier.create(subscription)
            .expectNext("{\"data\":{\"$query\":\"none\"}}")
            .expectNext("{\"data\":{\"$query\":\"value 2\"}}")
            .expectNext("{\"data\":{\"$query\":\"value3\"}}")
            .expectComplete()
            .verify()
    }

    @Test
    fun `verify subscriptionContext query with connectionParams read by MySubscriptionHooks`() {
        val query = "subscriptionContext"
        val subscription = subscribe(query, mapOf("Authorization" to "mytoken"))

        StepVerifier.create(subscription)
            .expectNext("{\"data\":{\"$query\":\"mytoken\"}}")
            .expectNext("{\"data\":{\"$query\":\"value 2\"}}")
            .expectNext("{\"data\":{\"$query\":\"value3\"}}")
            .expectComplete()
            .verify()
    }

    private fun subscribe(query: String, initPayload: Any? = null): TestPublisher<String> {
        val output = TestPublisher.create<String>()

        val client = ReactorNettyWebSocketClient()
        val uri = URI.create("ws://localhost:$port$SUBSCRIPTION_ENDPOINT")

        client.execute(uri) { session -> executeSubscription(session, initPayload, query, output) }.subscribe()

        return output
    }

    private fun executeSubscription(
        session: WebSocketSession,
        initPayload: Any?,
        query: String,
        output: TestPublisher<String>
    ): Mono<Void> {
        val id = Random.nextInt().toString()
        val initMessage = getInitMessage(id, initPayload)
        val startMessage = getStartMessage(query, id)

        return session.send(Flux.just(session.textMessage(initMessage)))
            .then(
                session.send(Flux.just(session.textMessage(startMessage)))
                    .thenMany(
                        session.receive()
                            .map { objectMapper.readValue<SubscriptionOperationMessage>(it.payloadAsText) }
                            .doOnNext {
                                if (it.type == ServerMessages.GQL_DATA.type) {
                                    val data = objectMapper.writeValueAsString(it.payload)
                                    output.next(data)
                                } else if (it.type == ServerMessages.GQL_COMPLETE.type) {
                                    output.complete()
                                }
                            }
                    )
                    .doOnError {
                        output.error(it)
                    }
                    .doOnComplete {
                        output.complete()
                    }
                    .then()
            )
    }

    private fun SubscriptionOperationMessage.toJson() = objectMapper.writeValueAsString(this)
    private fun getInitMessage(id: String, payload: Any?) = SubscriptionOperationMessage(GQL_CONNECTION_INIT.type, id = id, payload = payload).toJson()
    private fun getStartMessage(query: String, id: String): String {
        val request = GraphQLRequest("subscription { $query }")
        return SubscriptionOperationMessage(GQL_START.type, id = id, payload = request).toJson()
    }
}
