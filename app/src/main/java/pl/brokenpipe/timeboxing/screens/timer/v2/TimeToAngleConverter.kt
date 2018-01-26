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

import pl.brokenpipe.timeboxing.screens.timer.Time
import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide
import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide.RIGHT
import java.math.BigDecimal

/**
 * Created by wierzchanowskig on 11.08.2017.
 */
class TimeToAngleConverter {
    private val ONE_SECOND_ANGLE = 0.1

    fun convert(time: Time, clockSpinSide: ClockSpinSide): Double {
        val bdSeconds = BigDecimal.valueOf(time.getTotalSeconds())
        val bdRotation = BigDecimal.valueOf(180)
        val bdOneSecondAngle = BigDecimal.valueOf(ONE_SECOND_ANGLE)
        return bdSeconds.multiply(bdOneSecondAngle).add(bdRotation).toDouble()
    }

    fun convert(angle: Double, clockSpinSide: ClockSpinSide): Time {
        val bdAngle = if (clockSpinSide == RIGHT) BigDecimal.valueOf(angle) else
            BigDecimal.valueOf(360.0).minus(BigDecimal.valueOf(angle)).abs()
        val bdRotation = BigDecimal.valueOf(180)
        val bdOneSecondAngle = BigDecimal.valueOf(ONE_SECOND_ANGLE)
        return Time(bdAngle.add(bdRotation).div(bdOneSecondAngle).toLong())
    }
}