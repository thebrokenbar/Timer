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
    fun isEmitting3SameItemsIn3SecondsWithManySubscribers() {
        val test1 = countdown.start(3000, 1000).test()
        val test2 = countdown.resume().test()
        val test3 = countdown.resume().test()
        intervalScheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)

        test1.assertValues(2000, 1000, 0)
        test2.assertValues(2000, 1000, 0)
        test3.assertValues(2000, 1000, 0)
    }

    @Test
    fun isCompletingOldTimerWhenNewIsMade() {
        val test1 = countdown.start(3000, 1000).test()
        intervalScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS)
        val test2 = countdown.start(3000, 1000).test()
        intervalScheduler.advanceTimeBy(3000, TimeUnit.MILLISECONDS)

        test1.assertValues(2000)
        test2.assertValues(2000,1000,0)

    }
}