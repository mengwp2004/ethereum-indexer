package com.rarible.protocol.nft.core.service.policy

import com.rarible.blockchain.scanner.ethereum.model.EthereumLogStatus
import com.rarible.core.entity.reducer.service.EventApplyPolicy
import com.rarible.protocol.nft.core.model.EthereumEntityEvent

open class PendingEventApplyPolicy<T : EthereumEntityEvent<T>> : EventApplyPolicy<T> {

    override fun reduce(events: List<T>, event: T): List<T> {
        return events + event
    }

    override fun wasApplied(events: List<T>, event: T): Boolean {
        return findPendingEvent(events, event) != null
    }

    protected fun findPendingEvent(events: List<T>, event: T): T? {
        return events.firstOrNull { current ->
            current.log.status == EthereumLogStatus.PENDING && current.compareTo(event) == 0
        }
    }
}
