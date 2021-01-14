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

import com.expediagroup.graphql.directives.deprecatedDirectiveWithReason
import com.expediagroup.graphql.execution.GraphQLContext
import com.expediagroup.graphql.generator.SchemaGenerator
import com.expediagroup.graphql.generator.extensions.getDeprecationReason
import com.expediagroup.graphql.generator.extensions.getGraphQLDescription
import com.expediagroup.graphql.generator.extensions.getGraphQLName
import com.expediagroup.graphql.generator.extensions.getSimpleName
import com.expediagroup.graphql.generator.extensions.safeCast
import graphql.introspection.Introspection.DirectiveLocation
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLEnumValueDefinition
import kotlin.reflect.KClass

internal fun <Context : GraphQLContext>generateEnum(generator: SchemaGenerator<Context>, kClass: KClass<out Enum<*>>): GraphQLEnumType {
    val enumBuilder = GraphQLEnumType.newEnum()

    enumBuilder.name(kClass.getSimpleName())
    enumBuilder.description(kClass.getGraphQLDescription())

    generateDirectives(generator, kClass, DirectiveLocation.ENUM).forEach {
        enumBuilder.withDirective(it)
    }

    kClass.java.enumConstants.forEach {
        enumBuilder.value(getEnumValueDefinition(generator, it, kClass))
    }
    return generator.config.hooks.onRewireGraphQLType(enumBuilder.build()).safeCast()
}

private fun <Context : GraphQLContext>getEnumValueDefinition(generator: SchemaGenerator<Context>, enum: Enum<*>, kClass: KClass<out Enum<*>>): GraphQLEnumValueDefinition {
    val valueBuilder = GraphQLEnumValueDefinition.newEnumValueDefinition()
    val valueField = kClass.java.getField(enum.name)

    val name = valueField.getGraphQLName()
    valueBuilder.name(name)
    valueBuilder.value(name)

    generateEnumValueDirectives(generator, valueField).forEach {
        valueBuilder.withDirective(it)
    }

    valueBuilder.description(valueField.getGraphQLDescription())

    valueField.getDeprecationReason()?.let {
        valueBuilder.deprecationReason(it)
        valueBuilder.withDirective(deprecatedDirectiveWithReason(it))
    }

    return generator.config.hooks.onRewireGraphQLType(valueBuilder.build()).safeCast()
}
