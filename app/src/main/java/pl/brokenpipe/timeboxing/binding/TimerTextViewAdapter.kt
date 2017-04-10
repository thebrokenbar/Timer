package pl.brokenpipe.timeboxing.binding

import android.databinding.BindingAdapter
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView
import pl.brokenpipe.timeboxing.screens.timer.TimerTypefaceSpan
import pl.brokenpipe.timeboxing.ui.TimerTextView

/**
 * Created by wierzchanowskig on 04.04.2017.
 */

@BindingAdapter("android:text")
fun bindText(textView: TimerTextView, text: String) {
    val colonPosition: Int = text.indexOf(':')
    val stringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)

    val numbersTypeface: Typeface = Typeface.create("sans-serif", Typeface.BOLD)

    if (colonPosition < 0) {
        stringBuilder.setSpan(TimerTypefaceSpan("sans-serif-thin", Typeface.SANS_SERIF), 0, text.length,
                              Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    } else {
        val colonTypeface: Typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)

        stringBuilder.setSpan(TimerTypefaceSpan("sans-serif-thin", Typeface.SANS_SERIF), 0, colonPosition,
                              Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        stringBuilder.setSpan(TimerTypefaceSpan("sans-serif-thin", Typeface.SANS_SERIF), colonPosition, colonPosition+1,
                              Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        stringBuilder.setSpan(TimerTypefaceSpan("sans-serif-thin", Typeface.SANS_SERIF), colonPosition+1, text.length,
                              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    textView.text = stringBuilder
}