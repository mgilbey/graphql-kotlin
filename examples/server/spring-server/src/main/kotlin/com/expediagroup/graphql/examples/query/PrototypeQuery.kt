package com.expediagroup.graphql.examples.query

import org.slf4j.LoggerFactory

open class PrototypeQuery {

    private object PrototypeQuery {
        var created: Int = 0
    }

    init {
        LoggerFactory.getLogger("prototype query").info("created new PrototypeQuery ${PrototypeQuery.created}")
        PrototypeQuery.created += 1
    }

    fun prototypeQueryRoot(): PrototypeQueryInfo = PrototypeQueryInfo(this.hashCode(), PrototypeQuery.created)

    fun prototypeQueryRoot2(): PrototypeQueryInfo = PrototypeQueryInfo(this.hashCode(), PrototypeQuery.created)

    class PrototypeQueryInfo(val hashCode: Int, val createdCount: Int)

}
