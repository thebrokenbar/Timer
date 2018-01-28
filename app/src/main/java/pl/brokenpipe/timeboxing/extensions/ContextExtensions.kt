package pl.brokenpipe.timeboxing.extensions

import android.content.Context
import android.view.View
import pl.brokenpipe.timeboxing.TimeboxingApplication
import pl.brokenpipe.timeboxing.dagger.component.AppComponent

/**
 * Created by gwierzchanowski on 28.01.2018.
 */

fun Context.toTimeboxingApplication(): TimeboxingApplication {
    return this.applicationContext as TimeboxingApplication
}

fun Context.getAppComponent(): AppComponent {
    return this.toTimeboxingApplication().component
}
