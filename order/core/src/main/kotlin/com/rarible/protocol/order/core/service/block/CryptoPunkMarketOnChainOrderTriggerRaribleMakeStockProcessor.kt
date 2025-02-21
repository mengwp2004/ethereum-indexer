package com.rarible.protocol.order.core.service.block

import com.rarible.ethereum.listener.log.domain.LogEvent
import com.rarible.ethereum.log.LogEventsListener
import com.rarible.protocol.order.core.model.CryptoPunksAssetType
import com.rarible.protocol.order.core.model.OnChainOrder
import com.rarible.protocol.order.core.repository.order.OrderRepository
import com.rarible.protocol.order.core.service.OrderUpdateService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Listens for CryptoPunkMarket's native orders and calls recalculation of 'makeStock' of Rarible orders.
 *
 * If there is a Rarible sell order and the owner makes another sell order via the CryptoPunkMarket,
 * the Rarible order must become inactive (makeStock = 0) because the approval to the punk transfer proxy is expired
 * (only one sell order is supported in the CryptoPunkMarket contract).
 */
@Service
class CryptoPunkMarketOnChainOrderTriggerRaribleMakeStockProcessor(
    private val orderRepository: OrderRepository,
    private val orderUpdateService: OrderUpdateService
) : LogEventsListener {
    override fun postProcessLogs(logs: List<LogEvent>): Mono<Void> {
        return mono {
            logs.asSequence()
                .mapNotNull { it.data as? OnChainOrder }
                .filter { it.make.type is CryptoPunksAssetType }
                .forEach { onChainOrder ->
                    val type = onChainOrder.make.type as CryptoPunksAssetType
                    val token = type.token
                    val tokenId = type.tokenId
                    orderRepository
                        .findByTargetNftAndNotCanceled(onChainOrder.maker, token, tokenId)
                        .collect { orderUpdateService.updateMakeStock(it.hash) }
                }
        }.then()
    }
}
