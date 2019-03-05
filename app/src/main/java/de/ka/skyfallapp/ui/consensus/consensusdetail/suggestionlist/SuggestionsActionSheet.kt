package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import de.ka.skyfallapp.R


class SuggestionsActionSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_suggestions_action_sheet, container, false)

        view.findViewById<TextView>(R.id.suggestionActionEdit).setOnClickListener {

            dismiss()
        }

        view.findViewById<TextView>(R.id.suggestionActionDelete).setOnClickListener {
            dismiss()
        }

        return view
    }

    companion object {
        fun newInstance(): SuggestionsActionSheet = SuggestionsActionSheet()
    }


}