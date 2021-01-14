package com.expediagroup.graphql.examples.beans

import com.expediagroup.graphql.examples.context.MyGraphQLContext
import com.expediagroup.graphql.examples.query.PrototypeQuery
import com.expediagroup.graphql.spring.registration.QueryRegistration
import org.slf4j.LoggerFactory
import org.springframework.aop.scope.ScopedProxyFactoryBean
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.context.annotation.RequestScope
import kotlin.reflect.KClass

@Configuration
class QueryRegistrationBeans {

    @Bean
    fun prototypeQuery() = object: QueryRegistration<MyGraphQLContext, PrototypeQuery> {
        override fun getObjectType(): KClass<PrototypeQuery> = PrototypeQuery::class
        override fun getImplementation(context: MyGraphQLContext): PrototypeQuery = PrototypeQuery()
    }
}
