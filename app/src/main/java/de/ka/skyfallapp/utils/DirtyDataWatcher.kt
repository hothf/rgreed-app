package de.ka.skyfallapp.utils

import io.reactivex.subjects.PublishSubject

/**
 * Utility class for checking if any data is dirty and should be refetched.
 */
class DirtyDataWatcher {

    /**
     * Marks the given key as dirty.
     */
    fun markDirty(key: String, id: Int? = null) {
        subject.onNext(DirtyData(key, id))
    }

    /**
     * Subject for observing dirty data.
     */
    val subject: PublishSubject<DirtyData> = PublishSubject.create()

    /**
     * Contains a reference to dirty data.
     *
     * @param key the key referencing dirty data
     * @param id optional id of the dirty item
     */
    data class DirtyData(val key: String, val id: Int? = null)
}