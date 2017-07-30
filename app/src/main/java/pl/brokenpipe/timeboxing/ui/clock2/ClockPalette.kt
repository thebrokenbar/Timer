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

package pl.brokenpipe.timeboxing.ui.clock2

import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.PorterDuff.Mode.DST_IN
import android.graphics.PorterDuffXfermode

/**
 * Created by wierzchanowskig on 29.07.2017.
 */
class ClockPalette(attributes: ClockViewAttributes) {
    val facePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val handPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val faceBackgroundPaint = Paint()
    val faceDividersPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val faceDividersMaskPaint = Paint()
    val bitmapPaint = Paint()

    init {
        with(facePaint) {
            style = FILL
            color = attributes.primaryFaceColor
        }
        with(faceBackgroundPaint) {
            style = FILL
            color = attributes.backgroundColor
        }
        with(handPaint) {
            style = STROKE
            color = attributes.handColor
            strokeWidth = 2f
        }
        with(faceDividersPaint) {
            style = STROKE
            color = attributes.dividersColor
            strokeWidth = 1.5f
        }
        with(faceDividersMaskPaint) {
            style = FILL
            color = 0xffffffff.toInt()
            xfermode = PorterDuffXfermode(DST_IN)
        }
    }
}