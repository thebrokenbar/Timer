package pl.brokenpipe.timeboxing.arch

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
interface ViewModel<out VIEW : View, out VIEW_STATE : ViewState> {
    val view: VIEW
    val viewState: VIEW_STATE
}