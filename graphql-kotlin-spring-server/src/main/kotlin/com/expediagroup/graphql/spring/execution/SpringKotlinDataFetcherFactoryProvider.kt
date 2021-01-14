/*
 * Copyright 2020 Expedia, Inc
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

package com.expediagroup.graphql.spring.execution

import com.expediagroup.graphql.execution.GraphQLContext
import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import org.springframework.context.ApplicationContext
import kotlin.reflect.KFunction

/**
 * This provides a wrapper around the [SimpleKotlinDataFetcherFactoryProvider] to call the [SpringDataFetcher] on functions.
 * This allows you to use Spring beans as function arugments and they will be populated by the data fetcher.
 */
class SpringKotlinDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper,
    private val applicationContext: ApplicationContext
) : SimpleKotlinDataFetcherFactoryProvider(objectMapper) {
    override fun <Context : GraphQLContext>functionDataFetcherFactory(target: (context: Context) -> Any?, kFunction: KFunction<*>): DataFetcherFactory<Any?> =
        DataFetcherFactory { SpringDataFetcher(target, kFunction, objectMapper, applicationContext) }
}
