package featurea.math

class Line(val point1: Point, val point2: Point) {
    operator fun component1() = point1.x
    operator fun component2() = point1.y
    operator fun component3() = point2.x
    operator fun component4() = point2.y
}
