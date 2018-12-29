package com.pajato.argus.cli

import com.pajato.argus.core.video.*
import com.pajato.argus.core.video.UpdateType.*
import com.pajato.io.KFile
import com.pajato.io.createKotlinFile

const val dirArgName = "-dir="
const val errorArgName = "-errorFileName="
const val repoArgName = "-repoFileName="
const val promptArgName = "-prompt="

/**
 * The main invocation interface. Arguments:
 *
 * -dir: the directory path to contain the repository and error files. Defaults to the User home directory.
 * -errorFileName: the name of the file containing errors. Defaults to .argus-errors
 * -repoFileName: the repository file name. Defaults to .argus-repo
 * -prompt: the command line prompt. Defaults to '> '
 */
fun main(args: Array<String>) {
    fun getArg(name: String, default: String): String {
        args.forEach { arg ->
            if (arg.startsWith(name) && arg.length > name.length) return arg.substring(name.length, arg.length)
        }
        return default
    }

    val dir = getArg(dirArgName, getHomeDir())
    val repoFile: KFile = createKotlinFile(dir, getArg(repoArgName, ".argus-repo"))
    val errorFile: KFile = createKotlinFile(dir, getArg(errorArgName, ".argus-errors"))
    val runner = CommandRunner(repoFile, errorFile, getArg(promptArgName, "> "))

    if (runner.runCommandsFromConsole() == 0) println("OK") else println("Argus failed.")
}

expect fun readNextLine(prompt: String = ""): String?
expect fun redirectConsoleInput(file: KFile): Int
expect fun getHomeDir(): String

internal val archiveRE = Regex("([0-9]+) (true|false)")
internal val commandRE = Regex("(register|archive|update) (.+)")
internal val updateRE = Regex("([0-9]+) (Add|Remove|RemoveAll) ([a-zA-Z]+) (.+)")

/** Provide a command runner class to accept commands from a file or from the console. */
class CommandRunner(repoFile: KFile, private val errorFile: KFile, private val prompt: String = "") {
    private val useCase = VideoInteractor(Registrar(createEventStore(repoFile.path)))

    /** Read commands from standard in. */
    fun runCommandsFromConsole(): Int {
        tailrec fun runCommand(line: String?, action: (line: String) -> Unit) {
            line?.let { action(line) } ?: return
            runCommand(readNextLine(prompt), action)
        }

        runCommand(readNextLine(prompt)) {
            processCommand(it)
        }
        return exitCode()
    }

    /** Read commands from a file. */
    fun runCommandsFromFile(commandFile: KFile): Int {
        tailrec fun runCommand(list: List<String>, action: (line: String) -> Unit) {
            val line = if (list.isNotEmpty()) list[0] else null
            line?.let { action(line) } ?: return
            runCommand(list.subList(1, list.size), action)
        }

        runCommand(commandFile.readLines()) {
            processCommand(it)
        }
        return exitCode()
    }

    private fun processCommand(line: String) {
        class Command {
            val result = commandRE.matchEntire(line)
            val name = if (result != null) result.groupValues[1] else ""
            val rest = if (result != null) result.groupValues[2] else ""
        }
        class Archive(private val rest: String) {
            val result = archiveRE.matchEntire(rest)
            val videoId = if (result != null) result.groupValues[1].toInt() else -1
            val state = if (result != null) result.groupValues[2].toBoolean() else false

            fun run() {
                if (result != null)
                    useCase.archive(videoId, state)
                else
                    errorFile.appendText("Invalid archive command arguments: ($rest)!")
            }
        }
        class Update(private val rest: String) {
            val result = updateRE.matchEntire(rest)
            val videoId: Int = if (result != null) result.groupValues[1].toInt() else -1
            val attrName: String = if (result != null) result.groupValues[3] else ""
            val attrValue: String = if (result != null) result.groupValues[4] else ""
            val videoData: MutableSet<Attribute> = if (result != null)
                    AttributeFactory.create(attrName, attrValue)?.let { mutableSetOf(it) } ?: mutableSetOf()
                else
                    mutableSetOf()
            val updateType: UpdateType = if (result != null) valueOf(result.groupValues[2]) else Add

            fun run() {
                if (result != null)
                    useCase.update(videoId, videoData, updateType)
                else
                    errorFile.appendText("Invalid update command arguments: ($rest)!")
            }
        }
        val command = Command()

        // Note, using a when expression is preferred but will cause a false missed branch report from Jacoco so an
        // if-then-else kludge is used instead.
        when {
            command.name == "register" -> useCase.register(command.rest)
            command.name == "archive" -> Archive(command.rest).run()
            command.name == "update" -> Update(command.rest).run()
            else -> errorFile.appendText("Invalid command: ($line)!\n")
        }
    }

    private fun exitCode() = if (errorFile.size() > 0) -1 else 0
}
