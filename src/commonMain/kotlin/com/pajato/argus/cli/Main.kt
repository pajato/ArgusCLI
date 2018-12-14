package com.pajato.argus.cli

import com.pajato.io.KFile

expect fun readNextLine(): String?
expect fun sleep(delayInMilliseconds: Long)

/** Run the Argus CLI returning an exit status. */
fun run(commandFile: KFile, repoFile: KFile): Int {
    return 0
}