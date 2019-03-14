package de.ka.skyfallapp.utils

import io.reactivex.subjects.PublishSubject

/**
 * Listens for back press events, not triggered from the actual back button.
 */
class BackPressEventListener {

    val observableBackpress: PublishSubject<Boolean> = PublishSubject.create()

    /**
     * Called on a wanted back press.
     */
    fun onBack() {
        observableBackpress.onNext(true)
    }
}
