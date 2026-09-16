package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.FrameMetrics
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/** Repeatable emulator workload; timings are diagnostics, not a device-independent performance gate. */
@RunWith(AndroidJUnit4::class)
class PickerBenchmarkTest {
    @Test fun repeatedScrolling() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext,
            SettingsMainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        val frames = CopyOnWriteArrayList<Long>()
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            frames.add(metrics.getMetric(FrameMetrics.TOTAL_DURATION))
        }
        lateinit var recycler: RecyclerView
        try {
            instrumentation.runOnMainSync {
                activity.window.addOnFrameMetricsAvailableListener(listener, Handler(Looper.getMainLooper()))
                val picker = AppPickerPanelView(activity)
                activity.setContentView(picker)
                picker.loadApps()
                recycler = picker.findViewById(R.id.rvPickerGrid)
            }
            var count = 0
            val deadline = SystemClock.elapsedRealtime() + 15_000
            while (count == 0 && SystemClock.elapsedRealtime() < deadline) {
                instrumentation.runOnMainSync { count = recycler.adapter?.itemCount ?: 0 }
                SystemClock.sleep(100)
            }
            assertTrue("App list failed to load", count > 0)
            frames.clear()
            repeat(12) { index ->
                instrumentation.runOnMainSync { recycler.smoothScrollToPosition(if (index % 2 == 0) count - 1 else 0) }
                SystemClock.sleep(300)
            }
            val sorted = frames.sorted()
            instrumentation.sendStatus(0, android.os.Bundle().apply {
                putString("scroll_frames", sorted.size.toString())
                putString("scroll_over_16ms", sorted.count { it > 16_666_667 }.toString())
                putString("scroll_p95_ms", if (sorted.isEmpty()) "unavailable" else (sorted[((sorted.size - 1) * 0.95).toInt()] / 1_000_000.0).toString())
            })
        } finally {
            instrumentation.runOnMainSync { activity.window.removeOnFrameMetricsAvailableListener(listener); activity.finish() }
        }
    }
}
