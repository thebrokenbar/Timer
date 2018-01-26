/*
 * *
 *  * Copyright 2017 Grzegorz Wierzchanowski
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pl.brokenpipe.timeboxing.screens.timer.v2

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by wierzchanowskig on 11.08.2017.
 */
class TimerModelTest {

//    private lateinit var timerModel: TimerModel
//
//    private lateinit var testScheduler: TestScheduler
//
//    @Before
//    fun setUp() {
//        MockitoAnnotations.initMocks(this)
//        testScheduler = TestScheduler()
//        RxJavaPlugins.setNewThreadSchedulerHandler({ testScheduler })
//        timerModel = TimerModel()
//    }
//
//    @After
//    fun tearDown() {
//        RxJavaPlugins.reset()
//    }
//
//    @Test
//    fun timerIncrementsByTwoSecondsWhenSubscribedToObservableAfterTwoSeconds() {
//        //given
//        val test = timerModel.timeChanges().test()
//
//        //when
//        timerModel.startTimer()
//
//        //then
//        testScheduler.advanceTimeBy(1000L, MILLISECONDS)
//        test.assertValueAt(0, {
//            time -> time.getTotalSeconds() == 1L
//        })
//        testScheduler.advanceTimeBy(1000L, MILLISECONDS)
//        test.assertValueAt(1, {
//            time -> time.getTotalSeconds() == 2L
//        })
//        test.assertValueCount(2)
//        test.assertNoErrors()
//    }
//
//    @Test
//    fun timerIncrementsByTwoSecondsWhenSubscribedToObservableAfterOneSecondAndTwoSecondsPauseAndOneSecond() {
//        //given
//        val test = timerModel.timeChanges().test()
//
//        //when
//        timerModel.startTimer()
//
//        //then
//        testScheduler.advanceTimeBy(1000L, MILLISECONDS)
//        test.assertValueAt(0, {
//            time -> time.getTotalSeconds() == 1L
//        })
//
//        timerModel.pauseTimer()
//        testScheduler.advanceTimeBy(2000L, MILLISECONDS)
//
//        timerModel.startTimer()
//        testScheduler.advanceTimeBy(1000L, MILLISECONDS)
//        test.assertValueAt(1, {
//            time -> time.getTotalSeconds() == 2L
//        })
//        test.assertValueCount(2)
//        test.assertNoErrors()
//    }
}