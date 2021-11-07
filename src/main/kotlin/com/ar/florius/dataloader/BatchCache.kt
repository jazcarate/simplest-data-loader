package com.ar.florius.dataloader

import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Defer

class BatchCache<KEY, VALUE>(private val inner: (List<KEY>) -> AndThenable<List<VALUE?>>) : DataLoader<KEY, VALUE> {
    private val queue: MultiMap<KEY, Defer<VALUE?>> = MultiMap()
    private val cache: MutableMap<KEY, VALUE?> = mutableMapOf()

    override fun load(key: KEY): AndThenable<VALUE?> {
        val defer = Defer<VALUE?>()
        queue.addOne(key, defer)
        return defer
    }

    override fun dispatch() {
        if (queue.isEmpty()) return

        val keys = queue.keys

        queue.filterKeys(cache::containsKey)
            .forEach { (key, _) ->
                queue.remove(key)!!.forEach { it.push(cache[key]) }
            }

        val remaining = keys.toList().minus(cache.keys)
        if (remaining.isEmpty()) return

        inner(remaining).andAccept {
            val results = mapOf(*keys.zip(it).toTypedArray())
            cache.putAll(results)
            results.forEach { (key, value) ->
                queue.remove(key)!!.forEach { defer -> defer.push(value) }
            }
            dispatch()
        }
    }

}
