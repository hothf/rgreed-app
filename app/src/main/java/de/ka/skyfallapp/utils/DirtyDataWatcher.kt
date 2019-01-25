package de.ka.skyfallapp.utils

/**
 * Utility class for checking if any data is dirty and should be refetched.
 */
class DirtyDataWatcher {

    /**
     * A map containing the dirty flag of a given string key.
     */
    private val entries = mutableMapOf<String, Boolean>()

    /**
     * Checks if the given key has dirty files or not. If the key does exist, performs the given action and marks it
     * as non-dirty and returns true.
     * If it does not exist, returns false.
     */
    fun handleDirty(key: String, perform: () -> Unit): Boolean {
        val dirty = entries[key]
        if (dirty != null && dirty) {
            perform()
            entries[key] = false
            return true
        }
        return false
    }

    /**
     * Marks the given key as dirty.
     */
    fun markDirty(key: String) {
        entries[key] = true
    }
}


