package de.ka.rgreed.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.ka.rgreed.R
import de.ka.rgreed.repo.api.models.ConsensusResponse
import timber.log.Timber

object ShareUtils {

    /**
     * Shows a share chooser for a consensus.
     */
    fun showConsensusShare(context: Context, consensus: ConsensusResponse) {
        // note that currently the navigation framework could not extract the id string from strings.xml, this is why
        // it is hardcoded here AND in the navigation xml. Be aware!
        context.startActivity(
            Intent.createChooser(
                buildConsensusShareIntent(context, consensus),
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

        Timber.i("INTENT - Trying to view: $consensus")
        return intent
    }

    /**
     * Constructs an intent for sending a deeplink to a consensus.
     */
    private fun buildConsensusShareIntent(context: Context, consensus: ConsensusResponse): Intent {
        var shareText =
            String.format(
                context.getString(R.string.consensus_share_detail),
                consensus.title,
                "https://consensus.com/${consensus.id}"
            )
        if (consensus.finished) {
            shareText =
                String.format(
                    context.getString(R.string.consensus_share_finished_detail),
                    consensus.title,
                    "https://consensus.com/${consensus.id}"
                )
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        Timber.i("INTENT - Created a share text: $shareText")
        return sendIntent
    }
}