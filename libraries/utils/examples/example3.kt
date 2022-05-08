package featurea.utils.examples

import featurea.utils.log
import featurea.utils.replaceSuffix

fun test3() {
    log("11.06.2021 00:00:00".replaceSuffix("00:00", "24:00"))
}