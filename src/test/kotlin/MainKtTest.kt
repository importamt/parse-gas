import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

internal class MainKtTest {

    private val desktop = "/Users/importamt/Desktop"

    @Test
    fun getFiles() {
        val directory = File(desktop)
        println("Directory : $directory")
        directory.listFiles()!!.forEach { file -> println(file.name) }
    }

    @Test
    fun getContents() {
        val directory = File("/Users/importamt/Desktop")
        println("Directory : $directory")
        val files = directory.listFiles()!!.filter { file -> file.isFile }

        files.forEach { file ->
            if (!file.name.startsWith("AE")) {
                println("What is this? : ${file.name}")
                return@forEach
            }

            val queue = ConcurrentLinkedQueue<String>()
            val contents = ConcurrentLinkedQueue<String>()

            val parseThread = Runnable {
                while (true) {
                    val content = queue.poll()
                    if (content == null) {
                        Thread.sleep(0)
                    } else {
                        contents.add(parse(content))
                    }
                }
            }

            for (i in 0..4) {
                Thread(parseThread, "Parsing thread [$i]").start()
            }

            val lines = file.readLines()
            println("==========================================")
            println("START PARSING ${file.name} (${lines.size})")
            println("==========================================")
            lines.forEach { queue.add(it) }

            while (true) {
                if (lines.size <= contents.size) {
                    val writtenFile = File(desktop + "/parsed_${file.name}")
                    println("WRITTEN : ${writtenFile.absolutePath}")
                    val stringContents = contents.joinToString()
                    writtenFile.writeText(stringContents)
                    println("==========================================")
                    println("FINISH INDEX : ${contents.size}, ${LocalDateTime.now()}")
                    println("==========================================")
                    break
                } else {
                    println("WAITING INDEX : ${contents.size}, ${LocalDateTime.now()}")
                    Thread.sleep(1000)
                }
            }

            println("FINISH PARSING ${file.name}")
        }
    }

    private fun parse(line: String): String {
        val parsedLine = line.trim().split(" ")

        return "${parsedLine[0]},${
            parsedLine[2].substring(
                0,
                parsedLine[2].length
            )
        },${parsedLine[3].split("gas")[1].split("\\x00")[0]}"
    }
}