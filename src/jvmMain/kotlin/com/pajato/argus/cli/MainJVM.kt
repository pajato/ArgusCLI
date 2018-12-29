package com.pajato.argus.cli

import com.pajato.io.KFile
import java.io.File

actual fun readNextLine(prompt: String): String? {
    if (prompt.isNotEmpty()) print(prompt)
    return readLine()
}

actual fun redirectConsoleInput(file: KFile): Int {
    val inputFile = File(file.path)
    System.setIn(inputFile.inputStream())
    return 0
}

actual fun getHomeDir(): String = System.getProperty("user.home")
