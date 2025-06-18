package com.vizio.viziobleoobe.ble

enum class BleTvDpadSelection(val value: Int) {
    LEFT(0),
    RIGHT(1),
    UP(2),
    DOWN(3),
    OK(4);

    companion object {
        fun fromValue(value: Int): BleTvDpadSelection? {
            return BleTvDpadSelection.entries.find { it.value == value }
        }
    }

}