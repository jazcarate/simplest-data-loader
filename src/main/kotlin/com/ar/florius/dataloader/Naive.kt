package com.ar.florius.dataloader

import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Sync


class Naive<KEY, VALUE>(
    private val inner: (List<KEY>) -> AndThenable<List<VALUE?>>,
) : DataLoader<KEY, VALUE> {

    override fun load(key: KEY): AndThenable<VALUE?> {
        return inner(listOf(key)).andThen { Sync(it[0]) }
    }

    override fun dispatch() {
        // Do nothing interesting
    }
}

