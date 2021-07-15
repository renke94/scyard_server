package server

import server.Color.*

enum class Color(val color: String) {
    BLACK("black"),
    BLUE("blue"),
    GREEN("green"),
    METAL("metal"),
    ORANGE("orange"),
    PINK("pink"),
    RED("red"),
    WHITE("white")
}

val colors = listOf(BLACK, BLUE, GREEN, METAL, ORANGE, PINK, RED, WHITE)