package net.glazov.data.utils.paymentmanager

class InsufficientFundsException: Exception(
    "not enough funds to complete this operation"
)