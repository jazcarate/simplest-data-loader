package com.ar.florius.monad

import java.util.*
import java.util.function.Consumer

class Defer<T> : AndThenable<T> {
    private var dependencies: Queue<Consumer<T>> = LinkedList()

    fun push(value: T) {
        dependencies.forEach {
            it.accept(value)
        }
        dependencies.clear()
    }

    override fun <B> andThen(next: (input: T) -> AndThenable<B>): AndThenable<B> {
        val defer = Defer<B>()
        this.dependencies.offer(Consumer<T> { t -> next(t).andThen { defer.push(it); defer } })
        return defer
    }
}