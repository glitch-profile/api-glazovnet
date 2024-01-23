package net.glazov

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun main() {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    while (true) {
        val timeString = readln()
        val offsetDateTime = OffsetDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
        val timestamp = offsetDateTime.toEpochSecond()
        println(timestamp)
        val stringSelection = StringSelection(timestamp.toString())
        clipboard.setContents(stringSelection, null)
        println("----------")
    }
}