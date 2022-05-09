package featurea.datetime.examples

import featurea.datetime.nowInstant
import featurea.datetime.toUtcString

fun example1() {
    println(nowInstant().toUtcString())
}