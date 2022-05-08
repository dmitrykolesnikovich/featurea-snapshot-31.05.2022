package featurea.utils.examples

import featurea.utils.Encoding
import featurea.utils.encodeToByteArray
import featurea.utils.encodeToShortArray
import java.nio.charset.Charset

fun testEncodeToShortArray() {
    println("191d49c180aca2e5".encodeToShortArray(count = 8).joinToString())
    println("688df680e608608b".encodeToShortArray(count = 8).joinToString())
    println(shortArrayOf(12601, 12644, 13369, 25393, 14384, 24931, 24882, 25909).encodeToByteArray(Encoding.BIG_ENDIAN).toString(Charset.forName("ISO-8859-1")))
    println(shortArrayOf(13880, 14436, 26166, 14384, 25910, 12344, 13872, 14434).encodeToByteArray(Encoding.BIG_ENDIAN).toString(Charset.forName("ISO-8859-1")))
}
