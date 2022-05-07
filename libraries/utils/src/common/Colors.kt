package featurea.utils

import kotlin.random.Random.Default.nextFloat

object Colors {
    val aliceblue: Color by lazy { 0xF0F8FFFF.toColor("aliceblue") }
    val antiquewhite: Color by lazy { 0xFAEBD7FF.toColor("antiquewhite") }
    val aqua: Color by lazy { 0x00FFFFFF.toColor("aqua") }
    val aquamarine: Color by lazy { 0x7FFFD4FF.toColor("aquamarine") }
    val azure: Color by lazy { 0xF0FFFFFF.toColor("azure") }
    val beige: Color by lazy { 0xF5F5DCFF.toColor("beige") }
    val bisque: Color by lazy { 0xFFE4C4FF.toColor("bisque") }
    val blackColor: Color by lazy { 0x000000FF.toColor("black") }
    val blanchedalmond: Color by lazy { 0xFFEBCDFF.toColor("blanchedalmond") }
    val blueColor: Color by lazy { 0x0000FFFF.toColor("blue") }
    val blueviolet: Color by lazy { 0x8A2BE2FF.toColor("blueviolet") }
    val brownColor: Color by lazy { 0xA52A2AFF.toColor("brown") }
    val burlywood: Color by lazy { 0xDEB887FF.toColor("burlywood") }
    val cadetblue: Color by lazy { 0x5F9EA0FF.toColor("cadetblue") }
    val chartreuse: Color by lazy { 0x7FFF00FF.toColor("chartreuse") }
    val chocolate: Color by lazy { 0xD2691EFF.toColor("chocolate") }
    val coral: Color by lazy { 0xFF7F50FF.toColor("coral") }
    val cornflowerblue: Color by lazy { 0x6495EDFF.toColor("cornflowerblue") }
    val cornsilk: Color by lazy { 0xFFF8DCFF.toColor("cornsilk") }
    val crimson: Color by lazy { 0xDC143CFF.toColor("crimson") }
    val cyanColor: Color by lazy { 0x00FFFFFF.toColor("cyan") }
    val darkblue: Color by lazy { 0x00008BFF.toColor("darkblue") }
    val darkcyan: Color by lazy { 0x008B8BFF.toColor("darkcyan") }
    val darkgoldenrod: Color by lazy { 0xB8860BFF.toColor("darkgoldenrod") }
    val darkgray: Color by lazy { 0xA9A9A9FF.toColor("darkgray") }
    val darkgreen: Color by lazy { 0x006400FF.toColor("darkgreen") }
    val darkkhaki: Color by lazy { 0xBDB76BFF.toColor("darkkhaki") }
    val darkmagenta: Color by lazy { 0x8B008BFF.toColor("darkmagenta") }
    val darkolivegreen: Color by lazy { 0x556B2FFF.toColor("darkolivegreen") }
    val darkorange: Color by lazy { 0xFF8C00FF.toColor("darkorange") }
    val darkorchid: Color by lazy { 0x9932CCFF.toColor("darkorchid") }
    val darkred: Color by lazy { 0x8B0000FF.toColor("darkred") }
    val darksalmon: Color by lazy { 0xE9967AFF.toColor("darksalmon") }
    val darkseagreen: Color by lazy { 0x8FBC8FFF.toColor("darkseagreen") }
    val darkslateblue: Color by lazy { 0x483D8BFF.toColor("darkslateblue") }
    val darkslategray: Color by lazy { 0x2F4F4FFF.toColor("darkslategray") }
    val darkturquoise: Color by lazy { 0x00CED1FF.toColor("darkturquoise") }
    val darkviolet: Color by lazy { 0x9400D3FF.toColor("darkviolet") }
    val deeppink: Color by lazy { 0xFF1493FF.toColor("deeppink") }
    val deepskyblue: Color by lazy { 0x00BFFFFF.toColor("deepskyblue") }
    val dimgray: Color by lazy { 0x696969FF.toColor("dimgray") }
    val dodgerblue: Color by lazy { 0x1E90FFFF.toColor("dodgerblue") }
    val firebrick: Color by lazy { 0xB22222FF.toColor("firebrick") }
    val floralwhite: Color by lazy { 0xFFFAF0FF.toColor("floralwhite") }
    val forestgreen: Color by lazy { 0x228B22FF.toColor("forestgreen") }
    val fuchsia: Color by lazy { 0xFF00FFFF.toColor("fuchsia") }
    val gainsboro: Color by lazy { 0xDCDCDCFF.toColor("gainsboro") }
    val ghostwhite: Color by lazy { 0xF8F8FFFF.toColor("ghostwhite") }
    val goldColor: Color by lazy { 0xFFD700FF.toColor("gold") }
    val goldenrod: Color by lazy { 0xDAA520FF.toColor("goldenrod") }
    val grayColor: Color by lazy { 0x808080FF.toColor("gray") }
    val greenColor: Color by lazy { 0x008000FF.toColor("green") }
    val greenyellow: Color by lazy { 0xADFF2FFF.toColor("greenyellow") }
    val honeydew: Color by lazy { 0xF0FFF0FF.toColor("honeydew") }
    val hotpink: Color by lazy { 0xFF69B4FF.toColor("hotpink") }
    val indianred: Color by lazy { 0xCD5C5CFF.toColor("indianred") }
    val indigo: Color by lazy { 0x4B0082FF.toColor("indigo") }
    val ivory: Color by lazy { 0xFFFFF0FF.toColor("ivory") }
    val khaki: Color by lazy { 0xF0E68CFF.toColor("khaki") }
    val lavender: Color by lazy { 0xE6E6FAFF.toColor("lavender") }
    val lavenderblush: Color by lazy { 0xFFF0F5FF.toColor("lavenderblush") }
    val lawngreen: Color by lazy { 0x7CFC00FF.toColor("lawngreen") }
    val lemonchiffon: Color by lazy { 0xFFFACDFF.toColor("lemonchiffon") }
    val lightblue: Color by lazy { 0xADD8E6FF.toColor("lightblue") }
    val lightcoral: Color by lazy { 0xF08080FF.toColor("lightcoral") }
    val lightcyan: Color by lazy { 0xE0FFFFFF.toColor("lightcyan") }
    val lightgoldenrodyellow: Color by lazy { 0xFAFAD2FF.toColor("lightgoldenrodyellow") }
    val lightgreen: Color by lazy { 0x90EE90FF.toColor("lightgreen") }
    val lightgrey: Color by lazy { 0xD3D3D3FF.toColor("lightgrey") }
    val lightpink: Color by lazy { 0xFFB6C1FF.toColor("lightpink") }
    val lightsalmon: Color by lazy { 0xFFA07AFF.toColor("lightsalmon") }
    val lightseagreen: Color by lazy { 0x20B2AAFF.toColor("lightseagreen") }
    val lightskyblue: Color by lazy { 0x87CEFAFF.toColor("lightskyblue") }
    val lightslategray: Color by lazy { 0x778899FF.toColor("lightslategray") }
    val lightsteelblue: Color by lazy { 0xB0C4DEFF.toColor("lightsteelblue") }
    val lightyellow: Color by lazy { 0xFFFFE0FF.toColor("lightyellow") }
    val lime: Color by lazy { 0x00FF00FF.toColor("lime") }
    val limegreen: Color by lazy { 0x32CD32FF.toColor("limegreen") }
    val linen: Color by lazy { 0xFAF0E6FF.toColor("linen") }
    val magenta: Color by lazy { 0xFF00FFFF.toColor("magenta") }
    val maroon: Color by lazy { 0x800000FF.toColor("maroon") }
    val mediumaquamarine: Color by lazy { 0x66CDAAFF.toColor("mediumaquamarine") }
    val mediumblue: Color by lazy { 0x0000CDFF.toColor("mediumblue") }
    val mediumorchid: Color by lazy { 0xBA55D3FF.toColor("mediumorchid") }
    val mediumpurple: Color by lazy { 0x9370DBFF.toColor("mediumpurple") }
    val mediumseagreen: Color by lazy { 0x3CB371FF.toColor("mediumseagreen") }
    val mediumslateblue: Color by lazy { 0x7B68EEFF.toColor("mediumslateblue") }
    val mediumspringgreen: Color by lazy { 0x00FA9AFF.toColor("mediumspringgreen") }
    val mediumturquoise: Color by lazy { 0x48D1CCFF.toColor("mediumturquoise") }
    val mediumvioletred: Color by lazy { 0xC71585FF.toColor("mediumvioletred") }
    val midnightblue: Color by lazy { 0x191970FF.toColor("midnightblue") }
    val mintcream: Color by lazy { 0xF5FFFAFF.toColor("mintcream") }
    val mistyrose: Color by lazy { 0xFFE4E1FF.toColor("mistyrose") }
    val moccasin: Color by lazy { 0xFFE4B5FF.toColor("moccasin") }
    val navajowhite: Color by lazy { 0xFFDEADFF.toColor("navajowhite") }
    val navy: Color by lazy { 0x000080FF.toColor("navy") }
    val oldlace: Color by lazy { 0xFDF5E6FF.toColor("oldlace") }
    val olive: Color by lazy { 0x808000FF.toColor("olive") }
    val olivedrab: Color by lazy { 0x6B8E23FF.toColor("olivedrab") }
    val orangeColor: Color by lazy { 0xFFA500FF.toColor("orange") }
    val orangered: Color by lazy { 0xFF4500FF.toColor("orangered") }
    val orchid: Color by lazy { 0xDA70D6FF.toColor("orchid") }
    val palegoldenrod: Color by lazy { 0xEEE8AAFF.toColor("palegoldenrod") }
    val palegreen: Color by lazy { 0x98FB98FF.toColor("palegreen") }
    val paleturquoise: Color by lazy { 0xAFEEEEFF.toColor("paleturquoise") }
    val palevioletred: Color by lazy { 0xDB7093FF.toColor("palevioletred") }
    val papayawhip: Color by lazy { 0xFFEFD5FF.toColor("papayawhip") }
    val peachpuff: Color by lazy { 0xFFDAB9FF.toColor("peachpuff") }
    val peru: Color by lazy { 0xCD853FFF.toColor("peru") }
    val pink: Color by lazy { 0xFFC0CBFF.toColor("pink") }
    val plum: Color by lazy { 0xDDA0DDFF.toColor("plum") }
    val powderblue: Color by lazy { 0xB0E0E6FF.toColor("powderblue") }
    val purple: Color by lazy { 0x800080FF.toColor("purple") }
    val randomColor: Color get() = Color(nextFloat(), nextFloat(), nextFloat(), nextFloat())
    val redColor: Color by lazy { 0xFF0000FF.toColor("red") }
    val rosybrown: Color by lazy { 0xBC8F8FFF.toColor("rosybrown") }
    val royalblue: Color by lazy { 0x4169E1FF.toColor("royalblue") }
    val saddlebrown: Color by lazy { 0x8B4513FF.toColor("saddlebrown") }
    val salmon: Color by lazy { 0xFA8072FF.toColor("salmon") }
    val sandybrown: Color by lazy { 0xFAA460FF.toColor("sandybrown") }
    val seagreen: Color by lazy { 0x2E8B57FF.toColor("seagreen") }
    val seashell: Color by lazy { 0xFFF5EEFF.toColor("seashell") }
    val sienna: Color by lazy { 0xA0522DFF.toColor("sienna") }
    val silver: Color by lazy { 0xC0C0C0FF.toColor("silver") }
    val skyblue: Color by lazy { 0x87CEEBFF.toColor("skyblue") }
    val slateblue: Color by lazy { 0x6A5ACDFF.toColor("slateblue") }
    val slategray: Color by lazy { 0x708090FF.toColor("slategray") }
    val snow: Color by lazy { 0xFFFAFAFF.toColor("snow") }
    val springgreen: Color by lazy { 0x00FF7FFF.toColor("springgreen") }
    val steelblue: Color by lazy { 0x4682B4FF.toColor("steelblue") }
    val studioPanelColor: Color by lazy { 0xF6F6F6FF.toColor("studioPanel") }
    val tan: Color by lazy { 0xD2B48CFF.toColor("tan") }
    val teal: Color by lazy { 0x008080FF.toColor("teal") }
    val thistle: Color by lazy { 0xD8BFD8FF.toColor("thistle") }
    val tomato: Color by lazy { 0xFF6347FF.toColor("tomato") }
    val transparentColor: Color by lazy { 0x00000000.toColor("transparent") }
    val turquoise: Color by lazy { 0x40E0D0FF.toColor("turquoise") }
    val violetColor: Color by lazy { 0xEE82EEFF.toColor("violet") }
    val wheat: Color by lazy { 0xF5DEB3FF.toColor("wheat") }
    val whiteColor: Color by lazy { 0xFFFFFFFF.toColor("white") }
    val whitesmoke: Color by lazy { 0xF5F5F5FF.toColor("whitesmoke") }
    val yellowColor: Color by lazy { 0xFFFF00FF.toColor("yellow") }
    val yellowgreen: Color by lazy { 0x9ACD32FF.toColor("yellowgreen") }
}