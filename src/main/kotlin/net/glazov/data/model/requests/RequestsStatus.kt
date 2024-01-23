package net.glazov.data.model.requests

sealed class RequestsStatus{
    data object Active : RequestsStatus()
    data object InProgress : RequestsStatus()
    data object Solved : RequestsStatus()

    companion object {
        fun RequestsStatus.convertToIntCode(): Int {
            return when (this) {
                Active -> 0
                InProgress -> 1
                Solved -> 2
            }
        }
        fun getFromIntCode(typeCode: Int): RequestsStatus {
            return when (typeCode) {
                0 -> Active
                1 -> InProgress
                2 -> Solved
                else -> Active
            }
        }
    }

}
