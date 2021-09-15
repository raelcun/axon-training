package com.example.detgiftcarddemo.axon

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.LoggerFactory
import java.lang.Math.random
import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles

@Aggregate
class GiftCard {
    @AggregateIdentifier
    private var giftCardId: Long? = null
    private var remainingValue = 0

    protected constructor()

    @CommandHandler
    constructor(cmd: IssueCmd) {
        logger.debug("thread {}, handling {}", currentThread().id, cmd)
        require(cmd.amount > 0) { "amount <= 0" }
        apply(IssuedEvt(cmd.id, cmd.amount))
    }

    @CommandHandler
    fun handle(cmd: RedeemCmd) {
        logger.debug("thread {}, handling {}", currentThread().id, cmd)
        require(cmd.amount > 0) { "amount <= 0" }
        check(cmd.amount <= remainingValue) { "amount > remaining value" }
        apply(RedeemedEvt(giftCardId!!, cmd.amount))
    }

    @CommandHandler
    fun handle(cmd: CancelCmd?) {
        logger.debug("thread {}, handling {}", currentThread().id, cmd)
        apply(CancelEvt(giftCardId!!))
    }

    @EventSourcingHandler
    fun on(evt: IssuedEvt) {
        logger.debug("thread {}, applying {}", currentThread().id, evt)
        giftCardId = evt.id
        remainingValue = evt.amount
        logger.debug("new remaining value: {}", remainingValue)
    }

    @EventSourcingHandler
    fun on(evt: RedeemedEvt) {
        logger.debug("thread {}, applying {}", currentThread().id, evt)
        remainingValue -= evt.amount
        logger.debug("new remaining value: {}", remainingValue)
    }

    @EventSourcingHandler
    fun on(evt: CancelEvt?) {
        logger.debug("thread {}, applying {}", currentThread().id, evt)
        remainingValue = 0
        logger.debug("new remaining value: {}", remainingValue)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}