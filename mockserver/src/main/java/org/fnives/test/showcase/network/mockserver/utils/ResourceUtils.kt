package org.fnives.test.showcase.network.mockserver.utils

import java.io.BufferedReader
import java.io.InputStreamReader

internal fun Any.readResourceFile(filePath: String): String = try {
    BufferedReader(InputStreamReader(this.javaClass.classLoader.getResourceAsStream(filePath)!!))
        .readLines().joinToString("\n")
} catch (nullPointerException: NullPointerException) {
    throw IllegalArgumentException("$filePath file not found!", nullPointerException)
}

private fun BufferedReader.readLines(): List<String> {
    val result = mutableListOf<String>()
    use {
        do {
            readLine()?.let(result::add) ?: return result
        } while (true)
    }
}
