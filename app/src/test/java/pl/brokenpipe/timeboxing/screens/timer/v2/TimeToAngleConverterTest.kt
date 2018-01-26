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

import org.junit.Assert
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide

/**
 * Created by wierzchanowskig on 12.08.2017.
 */
class TimeToAngleConverterTest {
    lateinit var timeToAngleConverter: TimeToAngleConverter

    @Before
    fun setUp() {
        timeToAngleConverter = TimeToAngleConverter()
    }

    @Test
    fun timeIs0WhenConvertFromAngleWithAngle180() {
        //given
        val angle = 180.0
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertEquals(0, time.getTotalSeconds())
    }

    @Test
    fun timeIs2Hour0minutes0secondsWhenConvertedFromAngleWithAngle900() {
        //given
        val angle = 900.0
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertTrue(time.hours == 2L && time.minutes == 0L && time.seconds == 0L)
    }

    @Test
    fun angleIsTheSameWhenConvertedToTimeAndFomTime() {
        //given
        val angle = 173.4
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)
        val newAngle = timeToAngleConverter.convert(time, clockSpinSide)

        Assert.assertEquals(angle, newAngle, 0.001)
    }

    @Test
    fun timeIs15minutesWhenConvertingFromAngleWithAngle270AndSpinSideRight() {
        //given
        val angle = 270.0
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertEquals(0L, time.seconds)
        Assert.assertEquals(15L, time.minutes)
        Assert.assertEquals(0L, time.hours)
    }

    @Test
    fun timeIs15minutesWhenConvertingFromAngleWithAngle90AndSpinSideLeft() {
        //given
        val angle = 90.0
        val clockSpinSide = ClockSpinSide.LEFT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertEquals(0L, time.seconds)
        Assert.assertEquals(15L, time.minutes)
        Assert.assertEquals(0L, time.hours)
    }

    @Test
    fun timeIs45minutesWhenConvertingFromAngleWithAngle90AndSpinSideRight() {
        //given
        val angle = 90.0
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertEquals(0L, time.seconds)
        Assert.assertEquals(45L, time.minutes)
        Assert.assertEquals(0L, time.hours)
    }

    @Test
    fun timeIs45minutesWhenConvertingFromAngleWithAngle270AndSpinSideLeft() {
        //given
        val angle = 270.0
        val clockSpinSide = ClockSpinSide.RIGHT

        //when
        val time = timeToAngleConverter.convert(angle, clockSpinSide)

        Assert.assertEquals(0L, time.seconds)
        Assert.assertEquals(45L, time.minutes)
        Assert.assertEquals(0L, time.hours)
    }
}