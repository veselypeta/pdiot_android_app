package com.example.pdiot_cw3

import android.view.View
import android.view.ViewGroup
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@LargeTest
@RunWith(AndroidJUnit4::class)
class MyTestBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun buildParagraph() {
        benchmarkRule.measureRepeated {
            // measure cost of generating paragraph - this is overhead in the primary scroll()
            // benchmark, but is a very small fraction of the amount of work there.
//            buildRandomParagraph()
        }
    }

}

private fun ViewGroup.getLastChild(): View = getChildAt(childCount - 1)