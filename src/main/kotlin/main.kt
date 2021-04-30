import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private const val DESKTOP_DIRECTORY = "/Users/importamt/Desktop"
private const val THREAD_COUNT = 4

private var min: Long = Long.MAX_VALUE
private var max: Long = Long.MIN_VALUE


fun main(args: Array<String>) {

    println("Hello World!")
    getContents()

}

fun getContents() {

    val directory = File(DESKTOP_DIRECTORY)
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

        for (i in 0..THREAD_COUNT) {
            Thread(parseThread, "Parsing thread [$i]").start()
        }

        val lines = file.readLines()
        println("==========================================")
        println("START PARSING ${file.name} (${lines.size})")
        println("==========================================")
        lines.forEach { queue.add(it) }

        while (true) {
            if (lines.size <= contents.size) {
                val writtenFile = File(DESKTOP_DIRECTORY + "/parsed_${file.name}")
                println("WRITTEN : ${writtenFile.absolutePath}")
                val stringContents = contents.joinToString(System.lineSeparator())
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

    println("====================MIN, MAX====================")
    println("MIN : ${SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(min))}")
    println("MAX : ${SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(max))}")
    println("================================================")
}

private fun parse(line: String): String {
    val parsedLine = line.trim().split(" ")
    val timestamp = parsedLine[2].substring(10, parsedLine[2].length - 1).toLong()
    min = timestamp.coerceAtMost(min)
    max = timestamp.coerceAtLeast(max)

    return "${parsedLine[0]},${timestamp},${parsedLine[3].split("gas")[1].split("\\x00")[0]}"
}