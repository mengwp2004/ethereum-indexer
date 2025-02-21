package com.rarible.protocol.nft.core.service.policy

import com.rarible.blockchain.scanner.ethereum.model.EthereumLogStatus
import com.rarible.protocol.nft.core.data.createRandomMintItemEvent
import com.rarible.protocol.nft.core.data.withNewValues
import com.rarible.protocol.nft.core.model.ItemEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RevertEventApplyPolicyTest {
    private val revertEventApplyPolicy = RevertEventApplyPolicy<ItemEvent>()

    @Test
    fun `should remove confirm event`() {
        val mint = createRandomMintItemEvent().withNewValues(
            status = EthereumLogStatus.CONFIRMED,
            blockNumber = 4,
            logIndex = 1,
            minorLogIndex = 1,
        )
        val revertedMint = mint.withNewValues(
            status = EthereumLogStatus.REVERTED,
            blockNumber = 4,
            logIndex = 1,
            minorLogIndex = 1,
        )
        val events = (1L..3).map {
            createRandomMintItemEvent().withNewValues(
                status = EthereumLogStatus.CONFIRMED,
                blockNumber = it,
                logIndex = 1,
                minorLogIndex = 1,
            )
        }
        val wasApplied = revertEventApplyPolicy.wasApplied(events + mint, revertedMint)
        assertThat(wasApplied).isTrue

        val reduced = revertEventApplyPolicy.reduce(events + mint, revertedMint)
        assertThat(reduced).isEqualTo(events)
    }

    @Test
    fun `should say no if event was not applied`() {
        val mint = createRandomMintItemEvent().withNewValues(
            status = EthereumLogStatus.CONFIRMED,
            blockNumber = 4,
            logIndex = 1,
            minorLogIndex = 1,
        )
        val revertedMint = mint.withNewValues(
            status = EthereumLogStatus.REVERTED,
            blockNumber = 4,
            logIndex = 1,
            minorLogIndex = 1,
        )
        val events = (1L..3).map {
            createRandomMintItemEvent().withNewValues(
                status = EthereumLogStatus.CONFIRMED,
                blockNumber = it,
                logIndex = 1,
                minorLogIndex = 1,
            )
        }
        val wasApplied = revertEventApplyPolicy.wasApplied(events + mint, revertedMint)
        assertThat(wasApplied).isTrue

        val reduced = revertEventApplyPolicy.reduce(events + mint, revertedMint)
        assertThat(reduced).isEqualTo(events)
    }
}
