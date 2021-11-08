package com.ar.florius.monad

fun <A> wait(t: AndThenable<A>): A {
    val condition: Object = Object()
    var value: A? = null


    t.andAccept {
        synchronized(condition) {
            value = it
            condition.notifyAll()
        }
    }

    synchronized(condition) {
        condition.wait()
    }
    return value!!
}

fun <A, B> multi(p: Pair<AndThenable<A>, AndThenable<B>>): AndThenable<Pair<A, B>> {
    var a: A? = null
    var b: B? = null

    val d = Defer<Pair<A, B>>()

    p.first.andAccept {
        a = it
        if (b != null) d.push(a!! to b!!)
    }
    p.second.andAccept {
        b = it
        if (a != null) d.push(a!! to b!!)
    }

    return d
}