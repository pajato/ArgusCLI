package com.pajato.argus.cli

actual fun readNextLine(): String? = readLine()
actual fun sleep(delayInMilliseconds: Long) = Thread.sleep(delayInMilliseconds)
