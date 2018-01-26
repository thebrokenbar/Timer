package pl.brokenpipe.timeboxing.ui.clock.interfaces

import pl.brokenpipe.timeboxing.ui.clock.Side

interface ClockFaceActions {
    fun update()
    fun setClockFaceShape(angle: Double)
    fun setClockFacePaints(fullSpinsCount: Int)
    fun changeSide(side: Side)
}