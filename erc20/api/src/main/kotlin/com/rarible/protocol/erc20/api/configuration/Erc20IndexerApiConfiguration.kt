package com.rarible.protocol.erc20.api.configuration

import com.rarible.core.loggingfilter.EnableLoggingContextFilter
import com.rarible.core.mongo.configuration.EnableRaribleMongo
import com.rarible.ethereum.contract.EnableContractService
import com.rarible.ethereum.converters.EnableScaletherMongoConversions
import io.daonomic.rpc.mono.WebClientTransport
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import scalether.core.MonoEthereum
import scalether.domain.Address
import scalether.transaction.ReadOnlyMonoTransactionSender


@Configuration
@EnableRaribleMongo
@EnableContractService
@EnableLoggingContextFilter
@EnableScaletherMongoConversions
@EnableConfigurationProperties(Erc20IndexerApiProperties::class)
class Erc20IndexerApiConfiguration {

    @Bean
    fun sender(ethereum: MonoEthereum): ReadOnlyMonoTransactionSender {
        return ReadOnlyMonoTransactionSender(ethereum, Address.ZERO())
    }

    @Bean
    fun testEthereum(@Value("\${parityUrls}") url: String): MonoEthereum {
        return MonoEthereum(WebClientTransport(url, MonoEthereum.mapper(), 10000, 10000))
    }

    /*@Bean
    fun sender(ethereum: MonoEthereum) = ReadOnlyMonoTransactionSender(ethereum, Address.ZERO())
     */
}

