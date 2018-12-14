package com.pajato.argus.cli

import com.pajato.io.createKotlinFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplTest {
    @Test
    fun `read an empty command file and verify quit within one second`() {
        val job = GlobalScope.launch {
            delay(1000L)
        }

        val commandFile = createKotlinFile("src/commonTest/resources", "no-commands.txt")
        val repoFile = createKotlinFile("build", ".argus-repo")
        val exitCode = run(commandFile, repoFile)
        job.cancel()
        assertTrue(job.isCancelled, "The test run did not terminate!")
        assertEquals(0, exitCode, "Unexpected non-zero exit code!")
    }

    @Test
    fun `read an empty command file and verify no change to the repo file`() {
        val commandFile = createKotlinFile("src/commonTest/resources", "no-commands.txt")
        val repoFile = createKotlinFile("build", ".argus-repo")
        val currentSize = repoFile.size()
        val exitCode = run(commandFile, repoFile)
        assertEquals(currentSize, repoFile.size(), "The repo file has changed size!")
        assertEquals(0, exitCode, "Unexpected non-zero exit code!")
    }
}