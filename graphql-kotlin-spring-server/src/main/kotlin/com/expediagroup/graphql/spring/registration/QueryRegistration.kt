package com.expediagroup.graphql.spring.registration

import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.execution.GraphQLContext
import kotlin.reflect.KClass

interface QueryRegistration<in Context : GraphQLContext, QueryType : Any> {
    fun getImplementation(context: Context): QueryType
    fun getObjectType(): KClass<QueryType>
    fun toTopLeveLObject(): TopLevelObject<Context> = TopLevelObject({ context -> getImplementation(context)}, getObjectType())
}
