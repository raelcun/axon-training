package com.example.detgiftcarddemo.axon

import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data class CardSummary(val id: Long, val initialValue: Int, val remainingValue: Int) {
    companion object {
        fun fromIssuedEvent(issuedEvt: IssuedEvt) = CardSummary(issuedEvt.id, issuedEvt.amount, issuedEvt.amount)
    }
}

@Component
class CardSummaryProjection {
    var summaries: ConcurrentMap<Long, CardSummary> = ConcurrentHashMap()

    @EventHandler
    fun on(evt: IssuedEvt) {
        logger.trace("thread {}, projecting {}", currentThread().id, evt)
        
        summaries.put(evt.id, CardSummary.fromIssuedEvent(evt))
    }

    @EventHandler
    fun on(evt: CancelEvt) {
        logger.trace("thread {}, projecting {}", currentThread().id, evt)

        summaries.computeIfPresent(evt.id) { _, oldSummary -> CardSummary(oldSummary.id, oldSummary.initialValue, 0) }
    }

    @EventHandler
    fun on(evt: RedeemedEvt) {
        logger.trace("thread {}, projecting {}", currentThread().id, evt)

        summaries.computeIfPresent(evt.id) { _, oldSummary ->
            CardSummary(
                oldSummary.id,
                oldSummary.initialValue,
                oldSummary.remainingValue - evt.amount
            )
        }
    }

    @QueryHandler
    fun handle(query: FetchCardSummariesQuery): List<CardSummary> = summaries.values.toList()

    @QueryHandler
    fun handle(query: AuditSequenceQuery): MutableList<Long> {
        val failedAudit = mutableListOf<Long>()
        val maxId = summaries.keys.maxOrNull() ?: -1
        for (i in 1..maxId) if (!summaries.containsKey(i)) failedAudit.add(i)
        return failedAudit
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
}