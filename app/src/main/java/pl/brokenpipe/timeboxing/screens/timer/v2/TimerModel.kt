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

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.screens.timer.Time
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by wierzchanowskig on 11.08.2017.
 */

class TimerModel {
    private val timerFlowSpeed: Long = if (BuildConfig.FAST) 60 else 1

    private var timerDisposable: Disposable? = null
    private val timerThread = Schedulers.newThread()

    private val subject: PublishSubject<Long> = PublishSubject.create()

    var active = false
        private set

    fun startTimer() {
        timerDisposable?.dispose()
        timerDisposable = Observable.interval(1000 / timerFlowSpeed, MILLISECONDS, timerThread)
            .subscribe({ subject.onNext(it) }, { Timber.e(it) })
        active = true
    }

    fun pauseTimer() {
        timerDisposable?.dispose()
        timerDisposable = null
        active = false
    }

    @Suppress("USELESS_CAST")
    fun timeChanges() = subject as Observable<Long>
}