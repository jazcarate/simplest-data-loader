package com.ar.florius.monad

import io.kotest.core.spec.style.FunSpec

class DeferTest : FunSpec({

    test("andThen") {
        val a = Defer<Int>()
        val b = Defer<Int>()
        val c = Defer<Int>()

        a.andThen { aRes ->
            println("a got $aRes")
            b.andThen { bRes ->
                println("b got $bRes")
                c.andThen { cRes ->
                    println("c got $cRes")
                    Sync(aRes + bRes + cRes)
                }
            }
        }.andAccept {
            println("Outer got $it")
        }

        a.andAccept {
            println("a- got $it")
        }
        b.andAccept {
            println("b- got $it")
        }
        c.andAccept {
            println("c- got $it")
        }



        a.push(1)
        b.push(2)
        c.push(3)
    }
})
