package com.ar.florius

import com.ar.florius.dataloader.*
import com.ar.florius.dataloader.DataLoader
import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Sync

data class User(val id: Int, val name: String, val invitedBy: Int? = null)

class Database<KEY, VALUE>(private val db: Map<KEY, VALUE>) {
    fun loadMany(keys: List<KEY>): AndThenable<List<VALUE?>> {
        println("Fetching: $keys")
        return Sync(keys.map { db[it] })
    }
}

fun main() {
    val database = Database(
        mapOf(
            *listOf(
                User(1, "a"),
                User(2, "b"),
                User(5, "c", 1),
                User(6, "d", 5)
            ).map { it.id to it }.toTypedArray()
        )
    )
    val loader: DataLoader<Int, User> =
        //    Deferred(database::loadMany)
        // Batch(database::loadMany)
        BatchCache(database::loadMany)
        // NaiveLoader(database::loadMany)

    loader.load(6).andThen { user ->
        loader.load(user!!.invitedBy!!).andThen { invitedBy ->
            loader.load(invitedBy!!.invitedBy!!).andAccept { invitedBy2 ->
                println("${user.name} was invited by ${invitedBy.name} and in turn invited by ${invitedBy2!!.name}")
            }
        }
    }

    loader.load(5).andThen { user ->
        loader.load(user!!.invitedBy!!)
    }.andAccept { invitedBy ->
        println("5 was invited by ${invitedBy!!.name}")
    }

    loader.dispatch()
}