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

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.android.schedulers.AndroidSchedulers
import pl.brokenpipe.timeboxing.BR
import pl.brokenpipe.timeboxing.screens.timer.Time
import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide
import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide.LEFT_RIGHT
import timber.log.Timber

/**
 * Created by wierzchanowskig on 11.08.2017.
 */

class TimerViewModel(
    private val timeToAngleConverter: TimeToAngleConverter,
    private val timerModel: TimerModel
) : BaseObservable() {

    @get:Bindable
    var timerAngle: Double = 0.0
        set(value) {
            if (value == field) return
            field = value
            time.setTime(timeToAngleConverter.convert(field, clockSpinSide))
            notifyPropertyChanged(BR.timerAngle)
            Timber.i("Angle: $field")
        }

    @get:Bindable
    val time: Time = Time(0)

    @get:Bindable
    var clockSpinSide: ClockSpinSide = LEFT_RIGHT
    set(value) {
        if(field != value) notifyPropertyChanged(BR.clockSpinSide)
        field = value
    }

    @get:Bindable
    var timerLeftValueVisibility: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.timerLeftValueVisibility)
        }

    @get:Bindable
    var pauseButtonVisibility: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.pauseButtonVisibility)
        }

    @get:Bindable
    var timeLeftValue: String = "00"
        set(value) {
            field = value
            notifyPropertyChanged(BR.timeLeftValue)
        }

    @get:Bindable
    var timeRightValue: String = "00"
        set(value) {
            field = value
            notifyPropertyChanged(BR.timeRightValue)
        }

    init {
        timerModel.timeChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                           timerAngle = timeToAngleConverter.convert(time.decrementSecond(), clockSpinSide)
                       }, { Timber.e(it) })
        time.observe().subscribe({
                                     updateTime()
                                     notifyPropertyChanged(BR.time)
                                 }, {})
    }

    private fun updateTime() {
        timerLeftValueVisibility = time.hours > 0L || time.minutes > 0L
        pauseButtonVisibility = !timerModel.active && time.hours + time.minutes + time.seconds > 0L
        timeLeftValue = if (time.hours > 0) time.hoursToString() else time.minutesToString()
        timeRightValue = if (time.hours > 0) time.minutesToString() else time.secondsToString()
    }
}
