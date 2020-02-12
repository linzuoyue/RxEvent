package com.lzy.lib.rxevent;

import androidx.annotation.Nullable;

import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.HasUpstreamObservableSource;
import io.reactivex.internal.util.ArrayListSupplier;
import io.reactivex.subjects.BehaviorSubject;

/**
 * desc: Activity 生命周期 <br/>
 * author: 林佐跃 <br/>
 * date: 2019/1/9 <br/>
 * since V mello 2.3.0 <br/>
 */
public class ObservableActivityLifecyclePack<T> extends Observable<List<T>> implements HasUpstreamObservableSource<T> {

    private ObservableSource<T> mSource;
    private BehaviorSubject<ActivityEvent> mLifecycleSubject;
    private int maxCount;

    ObservableActivityLifecyclePack(ObservableSource<T> source, BehaviorSubject<ActivityEvent> lifecycleSubject, int maxCount) {
        mSource = source;
        mLifecycleSubject = lifecycleSubject;
        this.maxCount = maxCount;
    }

    @Override
    protected void subscribeActual(Observer<? super List<T>> observer) {
        ActivityLifecyclePackObserver<T> actual = new ActivityLifecyclePackObserver<>(observer, ArrayListSupplier.asCallable(), maxCount);
        if (actual.createBuffer()) {
            mSource.subscribe(actual);
        }
        mLifecycleSubject.safeSubscribe(new BaseEventObserver<ActivityEvent>() {
            @Override
            protected void onResponse(boolean isSuccess, ActivityEvent event) {
                actual.activityStatus = event;
                if (ActivityEvent.DESTROY.equals(event)) {
                    actual.dispose();
                    disposed();
                } else if (ActivityEvent.RESUME.equals(event)) {
                    actual.drain();
                }
            }
        });
    }

    @Override
    public ObservableSource<T> source() {
        return mSource;
    }


    static final class ActivityLifecyclePackObserver<T> implements Observer<T>, Disposable {


        /**
         * 当前activity的生命周期状态
         */
        volatile ActivityEvent activityStatus;

        final Observer<? super List<T>> actual;
        final Callable<List<T>> bufferSupplier;
        AtomicBoolean atomicBoolean;
        List<T> buffer;


        Disposable s;
        private int maxCount;

        ActivityLifecyclePackObserver(Observer<? super List<T>> actual, Callable<List<T>> bufferSupplier, int maxCount) {
            this.actual = actual;
            this.bufferSupplier = bufferSupplier;
            this.maxCount = maxCount;
            atomicBoolean = new AtomicBoolean(false);
        }

        boolean createBuffer() {
            List<T> b;
            try {
                b = ObjectHelper.requireNonNull(bufferSupplier.call(), "Empty buffer supplied");
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                buffer = null;
                if (s == null) {
                    EmptyDisposable.error(t, actual);
                } else {
                    s.dispose();
                    actual.onError(t);
                }
                return false;
            }

            buffer = b;

            return true;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;
                actual.onSubscribe(this);
            }
        }


        @Override
        public void dispose() {
            s.dispose();
        }

        @Override
        public boolean isDisposed() {
            return s.isDisposed();
        }

        void drain() {
            while (atomicBoolean.compareAndSet(false, true)) {
                List<T> b = buffer;
                if (b != null) {
                    if (activityStatus == ActivityEvent.RESUME) {
                        actual.onNext(b);
                        createBuffer();
                    }
                }
                atomicBoolean.compareAndSet(true, false);
            }
        }

        @Override
        public void onNext(T t) {
            while (atomicBoolean.compareAndSet(false, true)) {
                List<T> b = buffer;
                if (b != null) {
                    if (t != null) {
                        if (b.size() >= maxCount) {
                            b.remove(0);
                        }
                        b.add(t);
                    }

                    if (activityStatus == ActivityEvent.RESUME) {
                        actual.onNext(b);
                        createBuffer();
                    }
                }
                atomicBoolean.compareAndSet(true, false);
            }
        }

        @Override
        public void onError(Throwable t) {
            buffer = null;
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            List<T> b = buffer;
            buffer = null;
            if (b != null && !b.isEmpty()) {
                actual.onNext(b);
            }
            actual.onComplete();
        }
    }

}
