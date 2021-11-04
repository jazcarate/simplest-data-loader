package com.ar.florius.monad

interface AndThenable<A> {
    fun <B> andThen(next: (A) -> AndThenable<B>): AndThenable<B>
    fun andAccept(next: (A) -> Any?): AndThenable<Unit> {
        return this.andThen {
            next(it)
            Sync(Unit)
        }
    }
}