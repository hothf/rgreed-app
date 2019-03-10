package de.ka.skyfallapp.utils

import android.app.Activity
import android.content.Intent
import de.ka.skyfallapp.R
import timber.log.Timber

object ShareUtils {

    /**
     * Shows a share chooser for a consensus.
     */
    fun showConsensusShare(activity: Activity, id: String) {
        // note that currently the navigation framework could not extract the string from strings.xml, this is why
        // it is hardcoded here AND in the navigation xml. Be aware!
        val shareText = "https://consensus.com/$id"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        Timber.i("Created a link to $shareText")
        activity.startActivity(
            Intent.createChooser(
                sendIntent,
                activity.resources.getText(R.string.consensus_share_chooser_title)
            )
        )
    }

}