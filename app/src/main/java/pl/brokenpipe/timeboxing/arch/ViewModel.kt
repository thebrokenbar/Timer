package pl.brokenpipe.timeboxing.arch

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
abstract class ViewModel<out VIEW : View, out VIEW_STATE : ViewState>(
        protected val view: VIEW,
        protected val viewState: VIEW_STATE
)