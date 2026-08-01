package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class OilHealthPredictorTest {

    @Test
    fun `thermal stress treats duration input as seconds`() {
        val predictor = OilHealthPredictor()

        val result = predictor.analyze(
            OilHealthPredictor.OilHealthInput(
                oilTemp = 110.0,
                timeAbove110C = 3_600.0
            )
        )

        assertEquals(0.02, result.thermalStressIndex, 0.001)
    }
}
