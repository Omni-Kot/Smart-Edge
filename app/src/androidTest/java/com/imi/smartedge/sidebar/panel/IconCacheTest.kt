package com.imi.smartedge.sidebar.panel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IconCacheTest {
    @Test fun cacheWorkload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        AppRepository.clearSystemIconCache()
        val before = android.os.Debug.getPss()
        repeat(300) { index ->
            assertNotNull(repository.getProcessedIcon(context.packageName, "qa-$index"))
        }
        val field = AppRepository::class.java.getDeclaredField("iconCache").apply { isAccessible = true }
        val cache = field.get(null) as android.util.LruCache<*, *>
        val result = android.os.Bundle().apply {
            putString("cache_size", cache.size().toString())
            putString("cache_limit", cache.maxSize().toString())
            putString("pss_before_kb", before.toString())
            putString("pss_after_kb", android.os.Debug.getPss().toString())
        }
        InstrumentationRegistry.getInstrumentation().sendStatus(0, result)
        assertTrue(cache.size() <= cache.maxSize())
        AppRepository.clearSystemIconCache()
    }
}
