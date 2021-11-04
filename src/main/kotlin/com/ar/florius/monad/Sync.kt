package com.ar.florius.monad

class Sync<T>(private val value: T) : AndThenable<T> {
    override fun <B> andThen(next: (input: T) -> AndThenable<B>): AndThenable<B> {
        return next(value)
    }
}