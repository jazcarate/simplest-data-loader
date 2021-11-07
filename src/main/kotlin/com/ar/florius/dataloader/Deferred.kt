package com.ar.florius.dataloader

import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Defer


class Deferred<KEY, VALUE>(
    private val inner: (List<KEY>) -> AndThenable<List<VALUE?>>,
) : DataLoader<KEY, VALUE> {
    private val queue: MutableList<Pair<VALUE?, Defer<VALUE?>>> = mutableListOf()

    override fun load(key: KEY): AndThenable<VALUE?> {
        val defer = Defer<VALUE?>()
        inner(listOf(key)).andAccept { queue.add(it[0] to defer) }
        return defer
    }

    override fun dispatch() {
        val cloneQueue = ArrayList(queue)
        queue.clear()
        cloneQueue.forEach { (value, defer) ->
            defer.push(value)
            dispatch()
        }
    }
}

