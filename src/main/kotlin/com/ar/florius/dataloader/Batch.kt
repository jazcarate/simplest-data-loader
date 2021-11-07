package com.ar.florius.dataloader

import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Defer

class Batch<KEY, VALUE>(private val inner: (List<KEY>) -> AndThenable<List<VALUE?>>) : DataLoader<KEY, VALUE> {
    private val queue: MultiMap<KEY, Defer<VALUE?>> = MultiMap()

    override fun load(key: KEY): AndThenable<VALUE?> {
        val defer = Defer<VALUE?>()
        queue.addOne(key, defer)
        return defer
    }

    override fun dispatch() {
        if (queue.isEmpty()) return

        inner(queue.keys.toList()).andAccept {
            val results = mapOf(*queue.keys.zip(it).toTypedArray())
            results.forEach { (key, value) ->
                queue.remove(key)!!.forEach { defer -> defer.push(value) }
            }
            dispatch()
        }
    }

}
