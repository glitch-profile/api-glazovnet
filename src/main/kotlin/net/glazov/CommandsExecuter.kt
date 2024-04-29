package net.glazov

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val list1 = listOf(1,2,3,4,5)
    val available = listOf(1,2,3,4,5,6)
    val toSet = list1.filter { available.contains(it) }
    println(toSet)
}