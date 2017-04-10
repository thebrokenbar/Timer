package pl.brokenpipe.timeboxing.binding

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.ImageView

/**
 * Created by wierzchanowskig on 17.03.2017.
 */

@BindingAdapter("android:src")
fun bindImageViewSrc(iv: ImageView, drawable: Drawable) {
    iv.setImageDrawable(drawable)
}