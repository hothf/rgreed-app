package de.ka.rgreed.repo.db

import android.app.Application
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