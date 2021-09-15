package com.example.detgiftcarddemo

import com.example.detgiftcarddemo.axon.*
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture

@RestController
class Controllers @Autowired constructor(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {
    @PostMapping("/issue")
    fun issue(@RequestBody issueCmd: IssueCmd): ResponseEntity<Mono<Long>> = try {
        val start = Instant.now()
        val result: CompletableFuture<Long?> = commandGateway.send<Long?>(issueCmd).thenApply { e: Long ->
            val end = Instant.now()
            println(Duration.between(start, end).toMillis())
            e
        }.exceptionally { ex ->  null }

        ResponseEntity(Mono.fromFuture(result), HttpStatus.OK)
    } catch (err: CommandExecutionException) {
        ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/redeem")
    fun redeem(@RequestBody redeemCmd: RedeemCmd): ResponseEntity<Mono<RedeemCmd>> = try {
        ResponseEntity(Mono.fromFuture(commandGateway.send(redeemCmd)), HttpStatus.OK)
    } catch (err: CommandExecutionException) {
        ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/cancel")
    fun cancel(@RequestBody cancelCmd: CancelCmd): ResponseEntity<Mono<CancelCmd>> = try {
        ResponseEntity(Mono.fromFuture(commandGateway.send(cancelCmd)), HttpStatus.OK)
    } catch (err: CommandExecutionException) {
        ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/card-summaries")
    fun cardSummaries(): ResponseEntity<Mono<MutableList<CardSummary>>> = try {
        ResponseEntity(Mono.fromFuture(queryGateway.query(FetchCardSummariesQuery(), ResponseTypes.multipleInstancesOf(CardSummary::class.java))), HttpStatus.OK)
    } catch (err: CommandExecutionException) {
        ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/audit")
    fun audit(): ResponseEntity<Mono<MutableList<Long>>> = try {
        ResponseEntity(Mono.fromFuture(queryGateway.query(AuditSequenceQuery(), ResponseTypes.multipleInstancesOf(Long::class.java))), HttpStatus.OK)
    } catch (err: CommandExecutionException) {
        ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
