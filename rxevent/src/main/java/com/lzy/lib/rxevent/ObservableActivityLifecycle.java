package com.lzy.lib.rxevent;

import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.Nullable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.fuseable.HasUpstreamObservableSource;
import io.reactivex.internal.fuseable.SimpleQueue;
import io.reactivex.internal.observers.BasicFuseableObserver;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.BehaviorSubject;

/**
 * desc: Activity 生命周期 <br/>
 * author: 林佐跃 <br/>
 * date: 2019/1/9 <br/>
 * since V mello 2.3.0 <br/>
 */
public class ObservableActivityLifecycle<T> extends Observable<T> implements HasUpstreamObservableSource<T> {

    private ObservableSource<T> mSource;
    private BehaviorSubject<ActivityEvent> mLifecycleSubject;

    ObservableActivityLifecycle(ObservableSource<T> source, BehaviorSubject<ActivityEvent> lifecycleSubject) {
        mSource = source;
        mLifecycleSubject = lifecycleSubject;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        ActivityLifecycleObserver<T> actual = new ActivityLifecycleObserver<>(observer);
        mSource.subscribe(actual);
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


    static final class ActivityLifecycleObserver<T> extends BasicFuseableObserver<T, T> {

        /**
         * 数据队列
         */
        SimpleQueue<T> queue;

        /**
         * 错误
         */
        private Throwable error;
        /**
         * 是否取消了
         */
        private boolean cancelled;

        /**
         * 线程安全
         */
        AtomicInteger ai;

        /**
         * 当前activity的生命周期状态
         */
        ActivityEvent activityStatus;

        /**
         * Construct a BasicFuseableObserver by wrapping the given subscriber.
         *
         * @param actual the subscriber, not null (not verified)
         */
        ActivityLifecycleObserver(Observer<? super T> actual) {
            super(actual);
            ai = new AtomicInteger();

            queue = new SpscLinkedArrayQueue<T>(Flowable.bufferSize());
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            queue.offer(t);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
                return;
            }
            error = e;
            done = true;
            drain();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            drain();
        }

        /**
         *
         */
        private void drain() {
            if (ai.getAndIncrement() > 0) {
                return;
            }

            final SimpleQueue<T> q = queue;
            final Observer<? super T> a = actual;

            for (;;) {
                if (checkLifecycle() || checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                for (;;) {
                    boolean d = done;
                    T v;

                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        s.dispose();
                        q.clear();
                        a.onError(ex);
                        return;
                    }
                    boolean empty = v == null;

                    if (checkLifecycle() || checkTerminated(d, empty, a)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);
                }

                if (ai.addAndGet(-1) == 0) {
                    break;
                }
            }
        }

        /**
         * 检测生命周期是否可执行
         */
        private boolean checkLifecycle() {
            if (!activityStatus.equals(ActivityEvent.RESUME)) {
                ai.addAndGet(-1);
                return true;
            }
            return false;
        }

        /**
         * 线程控制，检查数据
         */
        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {
            if (cancelled) {
                queue.clear();
                return true;
            }
            if (d) {
                Throwable e = error;
                if (e != null) {
                    queue.clear();
                    a.onError(e);
                    return true;
                } else if (empty) {
                    a.onComplete();
                    return true;
                }
            }

            return false;
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                s.dispose();
//                if (ai.getAndIncrement() == 0) {
                    queue.clear();
//                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public T poll() throws Exception {
            return queue.poll();
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

}
