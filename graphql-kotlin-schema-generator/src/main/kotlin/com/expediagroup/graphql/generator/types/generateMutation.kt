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

package com.expediagroup.graphql.generator.types

import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.exceptions.ConflictingFieldsException
import com.expediagroup.graphql.exceptions.InvalidMutationTypeException
import com.expediagroup.graphql.execution.GraphQLContext
import com.expediagroup.graphql.generator.SchemaGenerator
import com.expediagroup.graphql.generator.extensions.getValidFunctions
import com.expediagroup.graphql.generator.extensions.isNotPublic
import graphql.introspection.Introspection.DirectiveLocation
import graphql.schema.GraphQLObjectType

internal fun <Context : GraphQLContext>generateMutations(generator: SchemaGenerator<Context>, mutations: List<TopLevelObject<Context>>): GraphQLObjectType? {

    if (mutations.isEmpty()) {
        return null
    }

    val mutationBuilder = GraphQLObjectType.Builder()
    mutationBuilder.name(generator.config.topLevelNames.mutation)

    for (mutation in mutations) {
        if (mutation.kClass.isNotPublic()) {
            throw InvalidMutationTypeException(mutation.kClass)
        }

        generateDirectives(generator, mutation.kClass, DirectiveLocation.OBJECT).forEach {
            mutationBuilder.withDirective(it)
        }

        mutation.kClass.getValidFunctions(generator.config.hooks)
            .forEach {
                val function = generateFunction(generator, it, generator.config.topLevelNames.mutation, mutation::getTarget)
                val functionFromHook = generator.config.hooks.didGenerateMutationField(mutation.kClass, it, function)
                if (mutationBuilder.hasField(functionFromHook.name)) {
                    throw ConflictingFieldsException("Mutation(class: ${mutation.kClass})", it.name)
                }
                mutationBuilder.field(functionFromHook)
            }
    }

    return generator.config.hooks.didGenerateMutationObject(mutationBuilder.build())
}
