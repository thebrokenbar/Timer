package pl.brokenpipe.timer.base;

import android.support.annotation.LayoutRes;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by wierzchanowskig on 18.02.2017.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Layout {
    @LayoutRes int value();
}
