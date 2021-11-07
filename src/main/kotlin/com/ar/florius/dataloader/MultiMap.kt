package com.ar.florius.dataloader

public class MultiMap<K, V> : LinkedHashMap<K, MutableList<V>>() {
    fun addOne(key: K, value: V) {
        if (this.containsKey(key)) {
            this[key]!!.add(value)
        } else {
            this[key] = mutableListOf(value)
        }
    }
}