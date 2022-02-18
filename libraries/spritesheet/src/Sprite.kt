package featurea.spritesheet

class Sprite(
    val spritesheet: Spritesheet,
    val path: String,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val u1: Float,
    val v1: Float,
    val u2: Float,
    val v2: Float,
) {
    val width: Float get() = x2 - x1
    val height: Float get() = y2 - y1
    val uv: List<Float> get() = listOf(u1, v1, u2, v2)
}
