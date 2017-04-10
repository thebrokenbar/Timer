package pl.brokenpipe.timeboxing.ui.clock.interfaces

import android.graphics.RectF

/**
 * Created by wierzchanowskig on 23.03.2017.
 */
interface ClockFaceActions {
    fun update()
    fun setClockFaceShape(angle: Float)
    fun setClockFacePaints(fullSpinsCount: Int)
}