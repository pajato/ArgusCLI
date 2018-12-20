package com.pajato.argus.cli

import com.pajato.io.createKotlinFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

val repoFile = createKotlinFile("build", ".argus-repo")
val errorFile = createKotlinFile("build", ".argus-errors")

internal fun runConsoleTest(): Int {
    repoFile.clear()
    errorFile.clear()
    val runner = CommandRunner(repoFile, errorFile)
    return runner.runCommandsFromConsole()
}

internal fun runTest(commandFileName: String): Int {
    val commandFile = createKotlinFile("src/commonTest/resources", commandFileName)
    repoFile.clear()
    errorFile.clear()
    val runner = CommandRunner(repoFile, errorFile)
    return runner.runCommandsFromFile(commandFile)
}

internal fun verifyNoErrors(originalSize: Int, exitCode: Int) {
    val expectedExitCode = if (errorFile.size() > 0) -1 else 0
    assertEquals(originalSize, errorFile.size(), "Unexpected errors:! See error file (${errorFile.path} for details.")
    assertEquals(expectedExitCode, exitCode, "Incorrect exit status!")
}

internal fun verifyErrors(originalSize: Int, exitCode: Int) {
    val expectedExitCode = if (errorFile.size() > 0) -1 else 0
    assertTrue(errorFile.size() > originalSize, "Expected some errors!")
    assertEquals(expectedExitCode, exitCode, "Incorrect exit status!")
}

class ReplTest {
    @Test
    fun `read an empty command file and verify quit within one second`() {
        val job = GlobalScope.launch { delay(1000L) }
        val exitCode = runTest("no-commands.txt")
        val errorFileSize = errorFile.size()
        job.cancel()
        assertTrue(job.isCancelled, "The test run did not terminate!")
        verifyNoErrors(errorFileSize, exitCode)
    }

    @Test
    fun `read an empty command file and verify no errors and no change to the repo file`() {
        val exitCode = runTest("no-commands.txt")
        assertEquals(0, repoFile.size(), "The repo file has changed size!")
        verifyNoErrors(0, exitCode)
    }

    @Test
    fun `read a non-empty invalid command file and verify some errors but no changes to the repo file`() {
        val exitCode = runTest("invalid-command.txt")
        assertEquals(0, repoFile.size(), "The repo file has changed size!")
        verifyErrors(0, exitCode)
    }

    @Test
    fun `read a register command and verify no errors and a changed repo file size`() {
        val exitCode = runTest("register-command.txt")
        assertNotEquals(0, repoFile.size(), "The repo file has not changed size!")
        verifyNoErrors(0, exitCode)
    }

    @Test
    fun `read an archive command and verify no errors and a changed repo file size`() {
        val exitCode = runTest("archive-command.txt")
        assertNotEquals(0, repoFile.size(), "The repo file has not changed size!")
        verifyNoErrors(0, exitCode)
    }

    @Test
    fun `read an archive command with an invalid video id and verify errors and a changed repo file size`() {
        val exitCode = runTest("archive-command-bad-video-id.txt")
        assertNotEquals(0, repoFile.size(), "The repo file has changed size!")
        verifyErrors(0, exitCode)
    }

    @Test
    fun `read an update command and verify no errors and a changed repo file size`() {
        val exitCode = runTest("update-command.txt")
        assertNotEquals(0, repoFile.size(), "The repo file has not changed size!")
        verifyNoErrors(0, exitCode)
    }

    @Test
    fun `drive command line input from redirected stdin using a valid file`() {
        val dir = "src/commonTest/resources"
        val name = "register-command.txt"
        val file = createKotlinFile(dir, name)
        redirectConsoleInput(file)
        val exitCode = runConsoleTest()
        assertNotEquals(0, repoFile.size(), "The repo file has not changed size!")
        verifyNoErrors(0, exitCode)
    }
}
