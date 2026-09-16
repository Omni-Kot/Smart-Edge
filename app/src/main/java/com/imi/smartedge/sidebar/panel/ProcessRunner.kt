package com.imi.smartedge.sidebar.panel

import java.io.InputStream
import java.util.concurrent.TimeUnit

/** Owns the process and drains both pipes, including when a command times out. */
object ProcessRunner {
    data class Result(val success: Boolean, val output: String)

    fun run(timeoutMs: Long = 5_000, start: () -> Process): Result {
        val process = try { start() } catch (_: Exception) { return Result(false, "") }
        val output = StringBuilder()
        fun drain(stream: InputStream, capture: Boolean) = Thread {
            try {
                stream.bufferedReader().use { reader ->
                    val buffer = CharArray(1024)
                    while (true) {
                        val count = reader.read(buffer)
                        if (count < 0) break
                        if (capture) synchronized(output) {
                            val remaining = 8192 - output.length
                            if (remaining > 0) output.append(buffer, 0, minOf(count, remaining))
                        }
                    }
                }
            } catch (_: Exception) { }
        }.apply { isDaemon = true; this.start() }
        val stdout = drain(process.inputStream, true)
        val stderr = drain(process.errorStream, false)
        return try {
            process.outputStream.close()
            // Poll exitValue: also works with Shizuku's remote Process implementation.
            val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
            var exit: Int? = null
            while (System.nanoTime() < deadline) {
                exit = try { process.exitValue() } catch (_: IllegalThreadStateException) { null }
                if (exit != null) break
                Thread.sleep(20)
            }
            if (exit == null) process.destroy()
            stdout.join(200)
            Result(exit == 0, synchronized(output) { output.toString() })
        } catch (_: Exception) {
            Result(false, "")
        } finally {
            process.destroy()
            runCatching { process.inputStream.close() }
            runCatching { process.errorStream.close() }
            stdout.interrupt()
            stderr.interrupt()
        }
    }
}
