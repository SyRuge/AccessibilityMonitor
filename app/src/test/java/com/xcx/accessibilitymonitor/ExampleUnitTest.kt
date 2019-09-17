package com.xcx.accessibilitymonitor

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    var b: Boolean = true

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun zzz() {
        var pd = "753708"
        val arr = pd.toCharArray()
        arr.forEach {
            val i = it.toInt()
            if (i == 7) {
                print("777 ")
            }
            println(i)
        }
    }

    @Test
    fun rrrr() {
        find(5)
    }


    fun find(i: Int) {

        if (i < 3) {
            println("break")

            for (i in 0..6) {

                if (!b) {
                    b = true
                    println()
                    find(5)
                }

                if (i == 4) {
                    return
                }
            }
        }

        println("after")
        find(i - 1)

    }

}
