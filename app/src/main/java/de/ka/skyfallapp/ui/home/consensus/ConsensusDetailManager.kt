package de.ka.skyfallapp.ui.home.consensus

import de.ka.skyfallapp.repo.api.ConsensusDetail

object ConsensusDetailManager {

    private var currentConsensusDetail: ConsensusDetail? = null

    fun setDetail(consensusDetail: ConsensusDetail?) {
        this.currentConsensusDetail = consensusDetail
    }

    fun getDetail(id: String?) =
        if (id != null && currentConsensusDetail?.id == id) this.currentConsensusDetail else null
}