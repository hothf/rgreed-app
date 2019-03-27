package de.ka.skyfallapp.utils

import android.content.Context
import android.content.Intent
import de.ka.skyfallapp.R
import timber.log.Timber

object ShareUtils {

    /**
     * Shows a share chooser for a consensus.
     */
    fun showConsensusShare(context: Context, id: String) {
        // note that currently the navigation framework could not extract the id string from strings.xml, this is why
        // it is hardcoded here AND in the navigation xml. Be aware!
        context.startActivity(
            Intent.createChooser(
                buildConsensusShareIntent(id),
                context.resources.getText(R.string.consensus_share_chooser_title)
            )
        )
    }

    /**
     * Constructs an intent for deeplinking to a consensus.
     */
    fun buildConsensusShareIntent(id: String?): Intent {
        var consensusId = "-1"

        if (id != null) {
            consensusId = id
        }
        val shareText = "https://consensus.com/$consensusId"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        Timber.i("Created a link to $shareText")
        return sendIntent
    }
}