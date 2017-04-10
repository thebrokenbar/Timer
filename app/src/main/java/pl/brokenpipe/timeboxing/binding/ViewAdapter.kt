package pl.brokenpipe.timeboxing.binding

import android.databinding.BindingAdapter
import android.view.View

/**
 * Created by wierzchanowskig on 04.04.2017.
 */

@BindingAdapter("android:visibility")
fun bindVisibility(view: View, isVisible: Boolean) {
    view.visibility = if(isVisible) View.VISIBLE else View.GONE
}