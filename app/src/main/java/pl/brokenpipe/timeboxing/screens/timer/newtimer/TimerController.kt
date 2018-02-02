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

package pl.brokenpipe.timeboxing.screens.timer.newtimer

import kotlinx.android.synthetic.main.testclock.*
import pl.brokenpipe.boundcontroller.BoundController
import pl.brokenpipe.boundcontroller.Layout
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.databinding.TestclockBinding
import pl.brokenpipe.timeboxing.extensions.getAppComponent
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerView
import javax.inject.Inject

/**
 * Created by wierzchanowskig on 30.07.2017.
 */
@Layout(R.layout.testclock)
class TimerController : BoundController<TestclockBinding>(), TimerView {

    @Inject
    protected lateinit var countdown: Countdown

    private val timerViewState: TimerViewState = TimerViewState()
    private var timerViewModel: TimerViewModel? = null

    override fun onViewBound(binding: TestclockBinding) {
        this.activity.getAppComponent().inject(this)

        timerViewModel = timerViewModel ?: TimerViewModel(this, timerViewState , countdown)
        binding.viewState = timerViewState
        binding.viewModel = timerViewModel
    }

    override fun onViewUnbound(binding: TestclockBinding) {

    }

    override fun showTimerEnd() {
        //TODO animation and sound
    }

    override fun showTimerFatalError() {
        //TODO Toast or snackbar
    }
}