package de.ka.rgreed.utils

import io.reactivex.subjects.PublishSubject

/**
 * Utility class for global messages.
 */
class GlobalMessageManager {

    data class GlobalMessage(val message: String)

    val observableGlobalMessage: PublishSubject<GlobalMessage> = PublishSubject.create()

    /**
     * Publishes a global message.
     *
     * @param message the message to publish
     */
    fun publishMessage(message: String) {
        observableGlobalMessage.onNext(GlobalMessage(message))
    }
}


