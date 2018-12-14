package com.pajato.argus.cli

actual fun readNextLine(): String? = readLine()

actual fun sleep(delayInMilliseconds: Long) {
    val delayInSeconds = (delayInMilliseconds + 1000L) / 1000L
    platform.posix.sleep(delayInSeconds.toUInt())
}
