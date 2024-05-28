package net.glazov.data.utils.paymentmanager

sealed class TransactionNoteTextCode(val code: String) {
    data object ConnectingNewService: TransactionNoteTextCode("new_service_connected")
    data object MonthlyPayment: TransactionNoteTextCode("monthly_payment")
    data class CustomNote(val text: String): TransactionNoteTextCode(text)
}
