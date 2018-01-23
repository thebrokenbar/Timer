package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit


class CountdownTest {
    private lateinit var countdown: Countdown
    private val intervalScheduler = TestScheduler()

    @Before
    fun setup() {
        RxJavaPlugins.setNewThreadSchedulerHandler { intervalScheduler }
        countdown = Countdown()
    }

    @Test
    fun isCompletingAfterCountingDownToZero() {
        val test = countdown.start(3000, 1000).test()
        intervalScheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)
        test.assertComplete()
    }

    @Test
    fun isEmitting3TimesAfter3SecondsOfCountingWithValidTime() {
        val test = countdown.start(3000, 1000).test()
        intervalScheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)

        test.assertValues(2000, 1000, 0)
    }

    @Test
    fun emittingTimeIsMutableByTimeIntervalMillisParameter() {
        val test = countdown.start(3000, 100).test()
        intervalScheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS)
        test.assertValueCount(3)
    }

    @Test
    fun isNotCompletingWhenCalledStopBeforeWholeTimeElapsed() {
        val test = countdown.start(3000, 1000).test()
        intervalScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS)
        countdown.stop()
        test.assertValueCount(2)
        test.assertNotComplete()
    }

    @Test
    fun isEmitting3TimesOn3SecondsWithManySubscribers() {
        val test1 = countdown.start(3000, 1000).test()
        val test2 = countdown.observe().test()
        val test3 = countdown.observe().test()
        intervalScheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)
        test1.assertValueCount(3)
        test2.assertValueCount(3)
        test3.assertValueCount(3)
    }
}