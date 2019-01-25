package de.ka.skyfallapp.repo.db

import android.app.Application
import io.objectbox.Box
import io.objectbox.BoxStore


/**
 * A object box database.
 */
class AppDatabase(application: Application) {

    private val db: BoxStore by lazy { MyObjectBox.builder().androidContext(application.applicationContext).build() }

    /**
     * Retrieve the object box.
     */
    fun get() = db
}

/**
 * Retrieves the update id of the first stored element. Intended to be help the usage when creating or updating
 * only one element.
 */
fun <T> Box<T>.singleUpdateId() =
    when (this.get(1L)) {
        null -> 0
        else -> 1L
    }