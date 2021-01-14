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

package com.expediagroup.graphql.generator.types

import com.expediagroup.graphql.execution.GraphQLContext
import com.expediagroup.graphql.generator.SchemaGenerator
import com.expediagroup.graphql.generator.extensions.getKClass
import com.expediagroup.graphql.generator.extensions.safeCast
import com.expediagroup.graphql.scalars.ID
import graphql.Scalars
import graphql.schema.GraphQLScalarType
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal fun <Context : GraphQLContext>generateScalar(generator: SchemaGenerator<Context>, type: KType): GraphQLScalarType? {
    val kClass: KClass<*> = type.getKClass()
    val scalar: GraphQLScalarType? = defaultScalarsMap[kClass]

    return scalar?.let {
        generator.config.hooks.onRewireGraphQLType(it).safeCast()
    }
}

private val defaultScalarsMap = mapOf(
    Int::class to Scalars.GraphQLInt,
    Long::class to Scalars.GraphQLLong,
    Short::class to Scalars.GraphQLShort,
    Float::class to Scalars.GraphQLFloat,
    Double::class to Scalars.GraphQLFloat,
    BigDecimal::class to Scalars.GraphQLBigDecimal,
    BigInteger::class to Scalars.GraphQLBigInteger,
    Char::class to Scalars.GraphQLChar,
    String::class to Scalars.GraphQLString,
    Boolean::class to Scalars.GraphQLBoolean,
    ID::class to Scalars.GraphQLID
)
