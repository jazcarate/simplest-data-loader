package com.ar.florius.dataloader


class Blocking<KEY, VALUE>(private val inner: (List<KEY>) -> List<VALUE?>) {
    private val condition: Object = Object()


    fun load(key: KEY): VALUE? {
        synchronized(condition) {
            condition.wait()
            return inner(listOf(key))[0]
        }
    }

    fun dispatch() {
        synchronized(condition) {
            condition.notifyAll()
        }
    }
}

