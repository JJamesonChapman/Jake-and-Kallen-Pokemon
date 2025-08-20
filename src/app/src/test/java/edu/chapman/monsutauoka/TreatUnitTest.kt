package edu.chapman.monsutauoka

import org.junit.Test

import org.junit.Assert.*
import kotlin.math.ceil


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TreatUnitTest {

    private fun endRound(tapCount:Int, targetTaps: Int): Int {

        val won = tapCount >= targetTaps
        if (won) {
            val awarded = ceil(targetTaps.toDouble() / 8).toInt()
            return awarded
        } else {
            return 0
        }
    }

    @Test
    fun test1() {
        val won = endRound(10, 8)
        assertEquals(won, 1)
    }

    @Test
    fun test2() {
        val won = endRound(11, 15)
        assertEquals(won, 0)
    }

    @Test
    fun test3() {
        val won = endRound(30, 30)
        assertEquals(won, 4)
    }
}
