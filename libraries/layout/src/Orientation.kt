package featurea.layout

val AllOrientations = listOf(
    Orientation.Portrait,
    Orientation.PortraitUpsideDown,
    Orientation.LandscapeRight,
    Orientation.LandscapeLeft
)

val LandscapeOrientations: List<Orientation> = listOf(Orientation.LandscapeLeft, Orientation.LandscapeRight)

val PortraitOrientations: List<Orientation> = listOf(Orientation.Portrait, Orientation.PortraitUpsideDown)

enum class Orientation {

    LandscapeLeft,
    LandscapeRight,
    Portrait,
    PortraitUpsideDown;

    val isHorizontal: Boolean get() = this == LandscapeRight || this == LandscapeLeft
    val isVertical: Boolean get() = this == Portrait || this == PortraitUpsideDown

}
