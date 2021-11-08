package com.ar.florius

import com.ar.florius.dataloader.BatchCache
import com.ar.florius.dataloader.DataLoader
import com.ar.florius.monad.AndThenable
import com.ar.florius.monad.Sync
import com.ar.florius.monad.multi
import com.ar.florius.monad.wait
import kotlin.concurrent.thread

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

    val invited2 = loader.load(6).andThen { user ->
        loader.load(user!!.invitedBy!!).andThen { invitedBy ->
            loader.load(invitedBy!!.invitedBy!!).andThen { invitedBy2 ->
                Sync(invitedBy2)
            }
        }
    }

    val user1 = loader.load(1)

    thread(start = true) {
        loader.dispatch()
    }

    val users = wait(
        multi(invited2 to user1)
    )
    println("1: ${users.first!!.name} & 2: ${users.second!!.name}")
}