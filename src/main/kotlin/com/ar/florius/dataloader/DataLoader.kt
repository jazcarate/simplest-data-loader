package com.ar.florius.dataloader

import com.ar.florius.monad.AndThenable

interface DataLoader<KEY, VALUE> {
    fun load(key: KEY): AndThenable<VALUE?>
    fun dispatch()
}