package com.lzy.lib.rxevent;


import androidx.annotation.NonNull;

import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * desc: Observable 的封装，减少模版代码<br/>
 * author: 林佐跃 <br/>
 * date: 2018/8/15 <br/>
 * since V mello 1.0.0 <br/>
 */
public abstract class BaseObservableWrap<T extends BaseEvent, W extends BaseObservableWrap> extends Observable<T> {

    @NonNull
    protected final ObservableSource<T> source;

    public BaseObservableWrap(@NonNull ObservableSource<T> source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(observer);
    }

    /**
     * Activity生命周期，帮助回收
     *
     * @return 返回处理好的Observable
     */
    public W recycle(@NonNull ActivityRx activity) {
        Observable<T> compose = compose(upstream -> {
            if (!ToolActivity.isActivityFinishing(activity)) {
                return upstream
                        .compose(activity.bindUntilEvent(ActivityEvent.DESTROY));
            }
            return upstream;
        });
        return wrap(compose);
    }

    /**
     * 只有在ActivityRx onResume的时候执行
     */
    public W filterLifecycleResume(@NonNull ActivityRx activity) {
        return filterLifecycle(activity, ActivityEvent.RESUME);
    }

    /**
     * 指定ActivityRx 状态执行
     */
    public W filterLifecycle(@NonNull ActivityRx activity, ActivityEvent event) {
        return wrap(filter(t ->  {
                    if (ToolActivity.isActivityFinishing(activity)) {
                        return false;
                    }
                    return activity.lifecycleSubject.getValue().equals(event);
                }));
    }

    /**
     * Activity生命周期
     */
    public W lifecycle(@NonNull ActivityRx activityRx) {
        return wrap(RxJavaPlugins.onAssembly(new ObservableActivityLifecycle<>(this, activityRx.lifecycleSubject)));
    }

    /**
     * Activity生命周期
     * @return
     */
    public Observable<List<T>> lifecyclePack(@NonNull ActivityRx activityRx) {
        return RxJavaPlugins.onAssembly(new ObservableActivityLifecyclePack<>(this, activityRx.lifecycleSubject));
    }

    /**
     * Fragment生命周期，帮助回收
     *
     * @return 返回处理好的Observable
     */
    public W recycle(@NonNull FragmentRx fragment) {
        Observable<T> compose = compose(upstream -> {
            if (!BusinessFragmentBase.isFragmentDeprecated(fragment)) {
                return upstream
                        .compose(fragment.bindUntilEvent(FragmentEvent.DESTROY));
            }
            return upstream;
        });
        return wrap(compose);
    }

    /**
     * 只有在FragmentRx onResume的时候执行
     */
    public W filterLifecycleResume(@NonNull FragmentRx fragment) {
        return filterLifecycle(fragment, FragmentEvent.RESUME);
    }

    /**
     * 指定FragmentRx 状态执行
     */
    public W filterLifecycle(@NonNull FragmentRx fragment, FragmentEvent event) {
        return wrap(filter(t ->  {
                    if (BusinessFragmentBase.isFragmentDeprecated(fragment)) {
                        return false;
                    }
                    return fragment.lifecycleSubject.getValue().equals(event);
                }));
    }

    /**
     * Fragment生命周期
     */
    public W lifecycle(@NonNull FragmentRx fragment) {
        return wrap(RxJavaPlugins.onAssembly(new ObservableFragmentLifecycle<>(this, fragment.lifecycleSubject)));
    }

    /**
     * 筛选{@link BaseEvent#extra} 数据 类型
     */
    public W filterExtraType(@NonNull Class<?> clz) {
        return wrap(filter(event -> {
            Object extra = event.getExtra();
            return clz.isInstance(extra);
        }));
    }

    protected abstract W wrap(@NonNull Observable<T> stream);

}
