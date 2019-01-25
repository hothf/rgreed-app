package de.ka.skyfallapp.utils

class BackPressInterceptor {

    var keyTag: String? = null
        private set(value) {
            if (field != value) {
                consumer = null
            }
            field = value
        }

    var consumer: (() -> Boolean)? = null
        private set

    /**
     * Intercepts a call with the given [consumer], or returns false, if none was specified
     */
    fun intercept() = consumer?.invoke() ?: false

    /**
     * Sets the [consumer] with a given tag.
     */
    fun setConsumer(key: String, block: () -> Boolean) {
        keyTag = key
        consumer = block
    }

    /**
     * Resets the [consumer], if associated with the given key
     */
    fun resetConsumer(key: String) {

        if (key == keyTag) {
            consumer = null
        }

    }

}
