package net.glazov.data.utils

sealed class RequestTariffsAccess {
    data object Default: RequestTariffsAccess()
    data object Organization: RequestTariffsAccess()
    data object Employee: RequestTariffsAccess()
}