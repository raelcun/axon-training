package com.example.detgiftcarddemo.axon

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class IssueCmd(@TargetAggregateIdentifier val id: Long, val amount: Int)
data class IssuedEvt(val id: Long, val amount: Int)
data class RedeemCmd(@TargetAggregateIdentifier val id: Long, val amount: Int)
data class RedeemedEvt(val id: Long, val amount: Int)
data class CancelCmd(@TargetAggregateIdentifier val id: Long)
data class CancelEvt(val id: Long)
class FetchCardSummariesQuery
class AuditSequenceQuery