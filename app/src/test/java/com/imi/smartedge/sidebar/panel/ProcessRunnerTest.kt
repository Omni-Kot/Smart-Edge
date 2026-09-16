package com.imi.smartedge.sidebar.panel

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProcessRunnerTest {
    private class FakeProcess(private val running: Boolean, text: String = "uid=0") : Process() {
        var destroyed = false
        private val input = ByteArrayInputStream(text.toByteArray())
        override fun getInputStream() = input
        override fun getErrorStream() = ByteArrayInputStream(ByteArray(128_000))
        override fun getOutputStream() = ByteArrayOutputStream()
        override fun waitFor() = error("Unbounded wait must not be called")
        override fun exitValue(): Int { if (running && !destroyed) throw IllegalThreadStateException(); return 0 }
        override fun destroy() { destroyed = true }
    }
    @Test fun timesOutAndDestroysProcess() {
        val process = FakeProcess(true)
        val start = System.nanoTime()
        assertFalse(ProcessRunner.run(50) { process }.success)
        assertTrue(process.destroyed)
        assertTrue((System.nanoTime() - start) / 1_000_000 < 2000)
    }
    @Test fun drainsOutputAndBoundsCapture() {
        val process = FakeProcess(false, "x".repeat(100_000))
        val result = ProcessRunner.run { process }
        assertTrue(result.success)
        assertEquals(8192, result.output.length)
        assertTrue(process.destroyed)
    }
    @Test fun failedStartReturnsFailure() {
        assertFalse(ProcessRunner.run { throw java.io.IOException("missing executable") }.success)
    }
}
