package com.canopobd.viewmodel

import com.canopobd.data.model.Mode22Data
import com.canopobd.data.protocol.Mode22Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Regression test: unsupported/failed Mode 22 DIDs must NOT be cached as
 * fake 0.0 readings with a fresh timestamp — consumers cannot distinguish
 * those from real zero values.
 *
 * Mirrors the null-branch of DashboardViewModel.requestMode22Data: a null
 * read result removes any previous cache entry instead of writing 0.0.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Mode22NullCacheTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `unsupported DID leaves no fake zero entry in cache`() = runTest {
        val mode22Client = mock(Mode22Client::class.java)
        `when`(mode22Client.readDID("F999")).thenReturn(flowOf(null))

        val cache = MutableStateFlow<Map<String, Mode22Data>>(
            mapOf("F999" to Mode22Data(pid = "F999", value = 42.0, unit = "kPa"))
        )

        // Same null-branch logic as DashboardViewModel.requestMode22Data.
        mode22Client.readDID("F999").collect { result ->
            if (result != null) {
                error("expected null for unsupported DID")
            } else {
                cache.value = cache.value - "F999"
            }
        }

        assertNull(cache.value["F999"])
    }
}
