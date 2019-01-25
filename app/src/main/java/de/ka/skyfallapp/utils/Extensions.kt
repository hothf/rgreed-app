package de.ka.skyfallapp.utils

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData

/**
 * Offers fast access to a default error handling of repo data.
 * For example, the profile fragment is immediately called if the given repo data say that the user is unauthorized.
 *
 * Will execute any given code that is not handled in the unhandled program block.
 */
fun <T> BaseViewModel.defaultErrorHandling(repoData: RepoData<T>, unhandled: (RepoData<T>) -> Unit) {
    if (repoData.info.code == 401) {
        navigateTo(R.id.profileFragment)
    } else {
        unhandled(repoData)
    }
}