package featurea.utils.examples

import featurea.utils.log
import featurea.utils.toIntRange

fun test2() {
    val result = "1..100".toIntRange()
    log(result)
}