package com.kevinkyang.inventory


class Utilities {
    companion object Math {
        fun formatFloat(num: Float): String {
            if (num.compareTo(num.toInt()) == 0) {
                return String.format("%d", num.toInt())
            }
            else {
                return String.format("%s", num)
            }
        }
    }
}