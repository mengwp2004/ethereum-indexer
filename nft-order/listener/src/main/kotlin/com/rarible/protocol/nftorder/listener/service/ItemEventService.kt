package com.rarible.protocol.nftorder.listener.service

import com.rarible.core.common.convert
import com.rarible.protocol.dto.NftItemDto
import com.rarible.protocol.nftorder.core.data.ItemEnrichmentData
import com.rarible.protocol.nftorder.core.event.ItemEvent
import com.rarible.protocol.nftorder.core.event.ItemEventDelete
import com.rarible.protocol.nftorder.core.event.ItemEventListener
import com.rarible.protocol.nftorder.core.event.ItemEventUpdate
import com.rarible.protocol.nftorder.core.model.Item
import com.rarible.protocol.nftorder.core.model.ItemId
import com.rarible.protocol.nftorder.core.service.ItemService
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Component
class ItemEventService(
    private val conversionService: ConversionService,
    private val itemService: ItemService,
    private val itemEventListeners: List<ItemEventListener>
) {

    private val logger = LoggerFactory.getLogger(ItemEventService::class.java)

    suspend fun onItemUpdated(nftItem: NftItemDto) {
        val rawItem = conversionService.convert<Item>(nftItem)
        val enrichmentData = itemService.getEnrichmentData(rawItem.id)
        onItemUpdated(rawItem, enrichmentData)
    }

    suspend fun onItemUpdated(rawItem: Item, data: ItemEnrichmentData) {
        val updated = itemService.enrichItem(rawItem, data)
        val existing = itemService.get(updated.id)
        if (data.isNotEmpty()) {
            updateItem(existing, updated, data)
        } else if (existing != null) {
            deleteItem(updated.id)
        }
        notify(ItemEventUpdate(updated))
    }

    private suspend fun updateItem(existing: Item?, updated: Item, data: ItemEnrichmentData) {
        if (existing == null) {
            logger.info(
                "Inserting Item [{}] with enrichment data: " +
                        "totalStock = [{}], bestSellOrder = [{}], bestBidOrder = [{}], unlockable = [{}]",
                updated.id, data.totalStock, data.bestSellOrder?.hash, data.bestBidOrder?.hash, data.unlockable
            )
            itemService.save(updated)
        } else {
            logger.info(
                "Updating Item [{}] with enrichment data: " +
                        "totalStock = [{}], bestSellOrder = [{}], bestBidOrder = [{}], unlockable = [{}]",
                updated.id, data.totalStock, data.bestSellOrder?.hash, data.bestBidOrder?.hash, data.unlockable
            )
            itemService.save(updated.copy(version = existing.version))
        }
    }

    private suspend fun deleteItem(itemId: ItemId) {
        val result = itemService.delete(itemId)
        if (result != null && result.deletedCount > 0) {
            logger.info("Deleted Item [{}] without enrichment data", itemId)
        }
    }

    suspend fun onItemDeleted(itemId: ItemId) {
        logger.info("Deleting Item [{}] since it was removed from NFT-Indexer", itemId)
        itemService.delete(itemId)
        notify(ItemEventDelete(itemId))
    }

    suspend fun onLockCreated(itemId: ItemId) {
        logger.info("Updating Item [{}] marked as Unlockable", itemId)
        val item = itemService.getOrFetchItemById(itemId).entity.copy(unlockable = true)
        itemService.save(item)
        notify(ItemEventUpdate(item))
    }

    private suspend fun notify(event: ItemEvent) {
        itemEventListeners.forEach { it.onEvent(event) }
    }
}