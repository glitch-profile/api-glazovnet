package net.glazov.data.utils.chatrequests

sealed class AlarmMessageTextCode(val code: String) {
    data object RequestClosed: AlarmMessageTextCode("request_closed")
    data object RequestReopened: AlarmMessageTextCode("request_reopened")
}