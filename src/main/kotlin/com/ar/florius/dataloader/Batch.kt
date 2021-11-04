package com.ar.florius.dataloader

import com.ar.florius.User
import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Defer

private class MultiMap<K, V> : LinkedHashMap<K, MutableList<V>>() {
    fun addOne(key: K, value: V) {
        if (this.containsKey(key)) {
            this[key]!!.add(value)
        } else {
            this[key] = mutableListOf(value)
        }
    }
}

class Batch(private val inner: (List<Int>) -> AndThenable<List<User?>>) : DataLoader<Int, User> {
    private val deps: MultiMap<Int, Defer<User?>> = MultiMap()
    private val cache: MutableMap<Int, User?> = mutableMapOf()

    override fun load(key: Int): AndThenable<User?> {
        val defer = Defer<User?>()
        deps.addOne(key, defer)
        return defer
    }

    override fun dispatch() {
        if (deps.isEmpty()) return

        val keys = deps.keys

        deps.filter { (key: Int, _: List<Defer<User?>>) -> cache.containsKey(key) }
            .forEach { (key: Int, _: List<Defer<User?>>) ->
                deps.remove(key)!!.forEach { it.push(cache[key]) }
            }

        inner(keys.toList().minus(cache.keys)).andAccept {
            val results = mapOf(*keys.zip(it).toTypedArray())
            cache.putAll(results)
            results.forEach { (key: Int, user: User?) ->
                deps.remove(key)!!.forEach { defer -> defer.push(user) }
            }
            dispatch()
        }
    }

}
