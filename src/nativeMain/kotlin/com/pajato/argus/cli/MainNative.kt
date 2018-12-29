package com.pajato.argus.cli

import com.pajato.io.KFile
import platform.posix.dup2
import platform.posix.fileno
import platform.posix.fopen

actual fun readNextLine(prompt: String): String? = readLine()
/*
actual fun sleep(delayInMilliseconds: Long) {
    val delayInSeconds = (delayInMilliseconds + 1000L) / 1000L
    platform.posix.sleep(delayInSeconds.toUInt())
}
*/
actual fun redirectConsoleInput(file: KFile): Int {
    val stdin = 0
    val fd: Int = fileno(fopen(file.path,"r"))
    if (fd < 0) return fd
    val result: Int = dup2(fd, stdin)
    return if (result < 0) result else 0
}

actual fun getHomeDir(): String {
    return "~/"
}