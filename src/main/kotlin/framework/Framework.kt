package framework

import java.io.File
import java.io.FileNotFoundException

object Framework {
    var dataFolder = "ccc_25"
    var prefix = "level"

    @JvmStatic
    fun readInputLines(level: Int, num: Int): List<String> =
        tryAccessFile("$dataFolder\\$prefix$level") { resolve("$prefix${level}_$num.in").readLines() }

    @JvmStatic
    fun readInput(level: Int, num: Int): String =
        tryAccessFile("$dataFolder\\$prefix$level") { resolve("$prefix${level}_$num.in").readText().trim() }

    @JvmStatic
    fun writeOutput(level: Int, num: Int, output: String) =
        tryAccessFile("$dataFolder\\$prefix$level") { resolve("$prefix${level}_$num.out").writeText(output) }

    /**
     * Try to access via gradle-set run directory, if not found, it means we are running from IDE configuration
     * and the "run" folder must be added to the path manually
     */
    private fun <T> tryAccessFile(name: String, block: File.() -> T): T {
        return try {
            File(name).block()
        } catch (e: FileNotFoundException) {
            File("run").resolve(name).block()
        }
    }
}
