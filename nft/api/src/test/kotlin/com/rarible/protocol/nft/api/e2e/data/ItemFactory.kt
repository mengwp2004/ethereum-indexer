package com.rarible.protocol.nft.api.e2e.data

import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomString
import com.rarible.ethereum.domain.EthUInt256
import com.rarible.protocol.nft.core.model.Item
import com.rarible.protocol.nft.core.model.ItemAttribute
import com.rarible.protocol.nft.core.model.ItemProperties
import scalether.domain.Address
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

fun createItem(): Item {
    val token = createAddress()
    val tokenId = EthUInt256.of(ThreadLocalRandom.current().nextLong(1, 10000))
    return Item(
        token = token,
        tokenId = tokenId,
        creators = listOf(createPart(), createPart()),
        supply = EthUInt256.of(ThreadLocalRandom.current().nextLong(1, 10000)),
        lazySupply = EthUInt256.of(ThreadLocalRandom.current().nextLong(1, 10000)),
        royalties = (1..ThreadLocalRandom.current().nextInt(1, 20)).map { createPart() },
        owners = (1..ThreadLocalRandom.current().nextInt(1, 20)).map { createAddress() },
        ownerships = (1..ThreadLocalRandom.current().nextInt(1, 20)).associate { createAddress() to EthUInt256.ONE },
        date = nowMillis()
    )
}

fun createLazyItemProperties() = ItemProperties(
    name = randomString(),
    description = randomString(),
    image = null,
    imagePreview = null,
    imageBig = null,
    animationUrl = null,
    attributes = listOf(
        ItemAttribute(
            key = randomString(),
            value = randomString(),
            type = randomString(),
            format = randomString()
        )
    ),
    rawJsonContent = null
)

fun createItem(
    token: Address,
    creator: Address?,
    owners: List<Address>,
    deleted: Boolean = false,
    date: Instant = nowMillis()
) = Item(
    token = token,
    tokenId = EthUInt256.of((1L..1000L).random()),
    creators = creator?.let { listOf(createPart().copy(account = it)) } ?: listOf(createPart(), createPart()),
    supply = EthUInt256.ONE,
    lazySupply = EthUInt256.ZERO,
    royalties = emptyList(),
    owners = owners,
    date = date,
    deleted = deleted
)
