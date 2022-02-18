package featurea

fun testNotNull(value: Any?, tag: String) {
    if (value != null) {
        println("$tag: success")
    } else {
        println("$tag: failure")
    }
}
