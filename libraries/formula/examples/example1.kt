package featurea.formula

import featurea.utils.log

fun test1() {
    val result = Formula<Boolean>("a < (b + 10) / 30").variables { "a" to 1; "b" to 10 }.calculate()
    log("Test1: $result")
}
