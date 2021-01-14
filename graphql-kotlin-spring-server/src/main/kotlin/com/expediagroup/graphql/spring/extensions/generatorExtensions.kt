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

package com.expediagroup.graphql.spring.extensions

import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.execution.GraphQLContext
import org.springframework.aop.support.AopUtils

/**
 * Convert a list of spring objects into a list of [TopLevelObject]s that
 * the schema generator can use
 */
internal fun List<Any>.toTopLevelObjects() = this.map {
    TopLevelObject<GraphQLContext>(it, AopUtils.getTargetClass(it).kotlin)
}
