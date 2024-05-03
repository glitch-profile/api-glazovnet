package net.glazov

fun main() {
    val list1 = listOf(1,2,3,4,5)
    val available = listOf(1,2,3,4,5,6)
    val toSet = list1.filter { available.contains(it) }
    println(toSet)
}