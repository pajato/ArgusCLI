package com.pajato.argus.cli

import org.junit.Test
import java.io.File
import kotlin.test.assertNotEquals

class JvmTest {
    @Test
    fun `drive command line input from redirected stdin`() {
        val file = File("src/commonTest/resources/register-command.txt")
        System.setIn(file.inputStream())
        val exitCode = runConsoleTest()
        assertNotEquals(0, repoFile.size(), "The repo file has not changed size!")
        verifyNoErrors(0, exitCode)
    }
}