package pl.brokenpipe.timeboxing.screens.timer.newtimer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import pl.brokenpipe.timeboxing.screens.timer.newtimer.exceptions.TimerDisposedException
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerView

/**
 * Created by gwierzchanowski on 26.01.2018.
 */
@RunWith(MockitoJUnitRunner::class)
class TimerViewModelTest {

    @Mock
    lateinit var timerView: TimerView

    @Mock
    lateinit var timerViewState: TimerViewState

    @Mock
    private lateinit var countdown: Countdown

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        viewModel = TimerViewModel(timerView, timerViewState, countdown)
    }

    @Test
    fun updateViewStateAnytimeWhenCountdownIsTicking() {
        whenever(countdown.start(4000, 1000))
                .thenReturn(Flowable.fromArray(3000, 2000, 1000, 0))
        whenever(timerViewState.timeInMillis).thenReturn(4000)

        viewModel.startTimer()

        verify(timerViewState, times(4)).timeInMillis = any()
    }

    @Test
    fun showFatalErrorViewWhenCountdownThrowsUnexpectedException() {
        whenever(countdown.start(any(), any()))
                .thenReturn(Flowable.error(RuntimeException()))
        whenever(timerViewState.timeInMillis).thenReturn(4000)

        viewModel.startTimer()

        verify(timerView).showTimerFatalError()
    }

    @Test
    fun setViewStateTimeToZeroWhenCountdownThrowsTimerDisposedException() {
        whenever(countdown.start(any(), any()))
                .thenReturn(Flowable.error(TimerDisposedException("")))
        whenever(timerViewState.timeInMillis).thenReturn(4000)

        viewModel.startTimer()

        verify(timerViewState).timeInMillis = 0
    }

    @Test
    fun viewStateSetsTimeOn1Hour20MinWhenPArsingFromAngle480(){
        viewModel.setTimeByAngle(660.0)
        verify(timerViewState).timeInMillis = (1 * 60 + 20) * 1000
    }

    @Test
    fun viewStateSetsSameTimeWhenParsedFromSameNegativeAndPositiveAngle(){
        viewModel.setTimeByAngle(660.0)
        viewModel.setTimeByAngle(-660.0)
        verify(timerViewState, times(2)).timeInMillis = (1 * 60 + 20) * 1000
    }

}