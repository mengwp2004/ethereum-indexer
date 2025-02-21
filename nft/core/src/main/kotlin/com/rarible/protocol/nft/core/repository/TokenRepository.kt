package com.rarible.protocol.nft.core.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.nft.core.model.Token
import com.rarible.protocol.nft.core.model.TokenFilter
import com.rarible.protocol.nft.core.model.TokenStandard
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import scalether.domain.Address

@Component
@CaptureSpan(type = SpanType.DB)
class TokenRepository(
    private val mongo: ReactiveMongoOperations
) {
    fun save(token: Token): Mono<Token> {
        return mongo.save(token)
    }

    fun remove(token: Address): Mono<Void> {
        val criteria = Criteria.where("_id").isEqualTo(token)
        return mongo.remove<Token>(Query(criteria)).then()
    }

    fun findAll(): Flux<Token> {
        return mongo.findAll()
    }

    fun count(): Mono<Long> {
        return mongo.count<Token>()
    }

    fun findById(id: Address): Mono<Token> {
        return mongo.findById(id)
    }

    fun findByStandard(standard: TokenStandard): Flux<Token> {
        val criteria = Criteria.where(Token::standard.name).isEqualTo(standard)
        return mongo.find(Query(criteria))
    }

    fun search(filter: TokenFilter): Flux<Token> {
        return mongo.find(filter.toQuery())
    }

    private fun TokenFilter.toQuery(): Query {
        val criteria = when (this) {
            is TokenFilter.All -> all()
            is TokenFilter.ByOwner -> byOwner(owner)
        }.and(Token::standard).ne(TokenStandard.NONE) scrollTo continuation

        return Query.query(criteria).limit(size)
    }

    private fun all() = Criteria()

    private fun byOwner(user: Address): Criteria =
        Criteria(Token::owner.name).`is`(user)

    private infix fun Criteria.scrollTo(continuation: String?): Criteria =
        if (continuation == null) {
            this
        } else {
            and(Token::id).lt(continuation)
        }
}
