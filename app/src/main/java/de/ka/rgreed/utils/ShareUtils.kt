package de.ka.rgreed.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.ka.rgreed.R
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
     * Constructs an intent for deeplinking to a consensus
     */
    fun buildConsensusViewIntent(id: String?): Intent {
        var consensusId = "-1"
        if (id != null) {
            consensusId = id
        }
        val consensus = "https://consensus.com/$consensusId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(consensus))

        Timber.i("INTENT - Trying to view $consensus")
        return intent
    }

    /**
     * Constructs an intent for sending a deeplink to a consensus.
     */
    private fun buildConsensusShareIntent(id: String?): Intent {
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
        Timber.i("INTENT - Created a link to $shareText")
        return sendIntent
    }
}