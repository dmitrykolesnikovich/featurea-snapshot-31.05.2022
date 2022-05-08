package featurea.utils.examples

import featurea.utils.splitWithWrappers

fun test1() {
    val tokens = "texture extractGif 'C:/Program Files (x86)/L-Control/Scada 2.0/Form Editor 2.0_exe/lcontrol/scada/studio/jumpRope.gif' 'lcontrol/scada/studio/jumpRope.gif'".splitWithWrappers(' ')
    for (token in tokens) log(token)
}
