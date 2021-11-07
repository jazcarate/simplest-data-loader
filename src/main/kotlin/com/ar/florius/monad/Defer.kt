package com.ar.florius.monad

class Defer<T> : AndThenable<T> {
    private var dependency: (T) -> Unit =
        {
            if (it !is Unit)
                println("WARN: There was nothing depending on this Defer")
        }

    fun push(value: T) {
        dependency(value)
    }

    override fun <B> andThen(next: (T) -> AndThenable<B>): AndThenable<B> {
        val defer = Defer<B>()
        this.dependency = { t -> next(t).andAccept(defer::push) }
        return defer
    }
}