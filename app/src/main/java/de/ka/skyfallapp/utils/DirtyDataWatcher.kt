package de.ka.skyfallapp.utils

import io.reactivex.subjects.PublishSubject

/**
 * Utility class for checking if any data is dirty and should be refetched.
 */
class DirtyDataWatcher {

    /**
     * Marks the given key as dirty.
     */
    fun markDirty(key: String) {
        subject.onNext(DirtyData(key))
    }

    /**
     * Subject for observing dirty data.
     */
    val subject: PublishSubject<DirtyData> = PublishSubject.create()

    /**
     * Contains a reference to dirty data.
     *
     * @param key the key referencing dirty data
     */
    data class DirtyData(val key: String)
}