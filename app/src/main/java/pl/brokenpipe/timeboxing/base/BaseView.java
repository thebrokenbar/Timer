package pl.brokenpipe.timeboxing.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;

/**
 * Created by wierzchanowskig on 09.11.2016.
 */

public abstract class BaseView<V extends ViewDataBinding> extends Controller {

    private static final int FADE_ANIMATION_DURATION = 100;
    private V binding;
    private OnViewResult onViewResultListener;

    public BaseView() {
    }

    public BaseView(Bundle args) {
        super(args);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        int layoutId = getLayoutId();
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        return binding.getRoot();
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        onViewBound(binding);
        binding.executePendingBindings();
    }

    private int getLayoutId() {
        Layout layoutAnnotation = this.getClass().getAnnotation(Layout.class);
        if (layoutAnnotation == null || layoutAnnotation.value() == 0) {
            throw new UnsupportedOperationException(
                "Cannot create view without layout resource annotated");
        }
        return layoutAnnotation.value();
    }

    protected void onViewBound(@NonNull V binding) {
    }

    protected void onViewUnbound(@NonNull V binding) {
    }

    @Override
    protected void onDetach(@NonNull View view) {
        super.onDetach(view);
        onViewUnbound(binding);
        binding.unbind();
    }

    public void setOnViewResultListener(OnViewResult onViewResult) {
        onViewResultListener = onViewResult;
    }

    protected void returnResult(int resultCode, Bundle params) {
        getRouter().popController(this);
        onViewResultListener.onResult(resultCode, params);
    }

    protected void showView(BaseView view) {
        view.setTargetController(this);
        getRouter().pushController(RouterTransaction.with(view)
            .pushChangeHandler(new FadeChangeHandler(FADE_ANIMATION_DURATION))
            .popChangeHandler(new FadeChangeHandler(FADE_ANIMATION_DURATION))
            .tag(view.getClass().getSimpleName()));
    }

    protected void showViewForResult(BaseView view, OnViewResult onViewResult) {
        view.setTargetController(this);
        showView(view);
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
