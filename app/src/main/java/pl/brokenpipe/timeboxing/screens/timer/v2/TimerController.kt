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

import android.view.View
import pl.brokenpipe.boundcontroller.BoundController
import pl.brokenpipe.boundcontroller.Layout
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.databinding.TestclockBinding
import kotlinx.android.synthetic.main.testclock.*
import pl.brokenpipe.timeboxing.ui.clock2.ClockView
import pl.brokenpipe.timeboxing.ui.clock2.OnClockFaceTouchListener

/**
 * Created by wierzchanowskig on 30.07.2017.
 */
@Layout(R.layout.testclock)
class TimerController : BoundController<TestclockBinding>(), TimerViewHandler {

    private lateinit var clockView: ClockView
    private val timerModel = TimerModel()
    private val timeToAngleConverter = TimeToAngleConverter()
    private val viewModel = TimerViewModel(timeToAngleConverter, timerModel)

    private fun assignViews() {
        clockView = activity?.clockView as ClockView
    }

    override fun onViewBound(binding: TestclockBinding) {
        binding.viewModel = viewModel
        binding.viewHandler = this
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        assignViews()
    }

    override fun onViewUnbound(binding: TestclockBinding) {

    }

    override fun startTimer() {
        timerModel.startTimer()
    }

    override fun pauseTimer() {
        timerModel.pauseTimer()
    }

    override fun onTimerTouchListener(): OnClockFaceTouchListener {
        return object: OnClockFaceTouchListener {
            override fun onHandleDragStart(angle: Double) {
                pauseTimer()
            }

            override fun onHandleDragStop(angle: Double) {
                startTimer()
            }
        }
    }
}