package com.rarible.protocol.nft.api.configuration

import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.ethereum.EnableEthereumScanner
import com.rarible.core.cache.EnableRaribleCache
import com.rarible.core.lockredis.EnableRaribleRedisLock
import com.rarible.core.loggingfilter.EnableLoggingContextFilter
import com.rarible.ethereum.nft.domain.EIP712DomainNftFactory
import com.rarible.ethereum.nft.validation.LazyNftValidator
import com.rarible.ethereum.sign.service.ERC1271SignService
import com.rarible.protocol.nft.core.model.ReduceSkipTokens
import io.daonomic.rpc.mono.WebClientTransport
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import scalether.core.EthPubSub
import scalether.core.MonoEthereum
import scalether.core.PubSubTransport
import scalether.domain.Address
import scalether.transaction.MonoTransactionPoller
import scalether.transaction.MonoTransactionSender
import scalether.transaction.ReadOnlyMonoTransactionSender
import scalether.transport.WebSocketPubSubTransport
import java.math.BigInteger

@EnableMongock
@Configuration
@EnableConfigurationProperties(NftIndexerApiProperties::class)
@EnableRaribleCache
@EnableLoggingContextFilter
@EnableRaribleRedisLock
@EnableEthereumScanner
class NftIndexerApiConfiguration(
    private val nftIndexerApiProperties: NftIndexerApiProperties
) {
    @Bean
    fun reduceSkipTokens(): ReduceSkipTokens {
        return ReduceSkipTokens.NO_SKIP_TOKENS
    }

    @Bean
    fun operatorProperties(): NftIndexerApiProperties.OperatorProperties {
        return nftIndexerApiProperties.operator
    }

    @Bean
    fun erc1271SignService(sender: MonoTransactionSender): ERC1271SignService {
        return ERC1271SignService(sender)
    }

    @Bean
    fun daonomicLazyNftValidator(sender: MonoTransactionSender, erc1271SignService: ERC1271SignService): LazyNftValidator {
        return LazyNftValidator(
            erc1271SignService,
            EIP712DomainNftFactory(BigInteger.valueOf(nftIndexerApiProperties.chainId))
        )
    }

    @Bean
    fun testEthereum(@Value("\${parityUrls}") url: String): MonoEthereum {
        return MonoEthereum(WebClientTransport(url, MonoEthereum.mapper(), 10000, 10000))
    }

    @Bean
    fun testSender(ethereum: MonoEthereum) = ReadOnlyMonoTransactionSender(ethereum, Address.ONE())

    @Bean
    fun poller(ethereum: MonoEthereum): MonoTransactionPoller {
        return MonoTransactionPoller(ethereum)
    }

    @Bean
    fun pubSubTransport(@Value("\${parityWebSocketUrls}") url: String): WebSocketPubSubTransport {
        return WebSocketPubSubTransport(url, Int.MAX_VALUE)
    }

    @Bean
    fun ethPubSub(transport: PubSubTransport): EthPubSub {
        return EthPubSub(transport)
    }

}
