package pl.brokenpipe.timeboxing.ui.clock.interfaces

import android.graphics.RectF
import pl.brokenpipe.timeboxing.ui.clock.Side

/**
 * Created by wierzchanowskig on 23.03.2017.
 */
interface ClockFaceActions {
    fun update()
    fun setClockFaceShape(angle: Float)
    fun setClockFacePaints(fullSpinsCount: Int)
    fun setClockFaceSide(side: Side)
}