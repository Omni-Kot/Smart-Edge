package com.imi.smartedge.sidebar.panel

import androidx.recyclerview.widget.RecyclerView
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class PanelAdapterTest {
    @Test fun updatesPreserveAddButtonAndUseSpecificEvents() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val adapter = PanelAppsAdapter(instrumentation.targetContext, {}, {}, {}, {}, {})
            var fullRefreshes = 0
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() { fullRefreshes++ }
            })
            val first = AppInfo("example.first", "First")
            val second = AppInfo("example.second", "Second")
            adapter.submitList(listOf(first, second))
            adapter.setShowAddButton(true)
            assertEquals(3, adapter.itemCount)
            adapter.moveItem(0, 1)
            assertEquals(listOf(second, first), adapter.currentList)
            adapter.submitList(listOf(first.copy(appName = "Renamed")))
            assertEquals(2, adapter.itemCount)
            assertEquals("Renamed", adapter.currentList.single().appName)
            assertNotEquals(adapter.getItemViewType(0), adapter.getItemViewType(1))
            adapter.setShowAddButton(false)
            assertEquals(1, adapter.itemCount)
            adapter.submitList(emptyList())
            assertEquals(0, adapter.itemCount)
            assertEquals(0, fullRefreshes)
        }
    }
}
