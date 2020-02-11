package com.lzy.lib.rxevent;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * desc: 事件总线基类 <br/>
 * author: 林佐跃 <br/>
 * date: 2018/8/14 <br/>
 * since V mello 1.0.0 <br/>
 */
public abstract class BaseEventManager<T extends BaseEvent, M extends BaseObservableWrap> implements Observer<T> {


    public final Subject<T> mSubject;

    public BaseEventManager() {
        // PublishSubject 从那里订阅就从那里开始发送数据
        // note
        // AsyncSubject 无论输入多少参数，永远只输出最后一个参数;
        // BehaviorSubject 会发送离订阅最近的上一个值，没有上一个值的时候会发送默认值;
        // ReplaySubject 都会将所有历史订阅内容全部发出。
        PublishSubject<T> objectPublishSubject = PublishSubject.create();
        // SerializedSubject 对Subject 的封装。并发处理，并发时只允许一个线程调用onNext等方法！
        mSubject = objectPublishSubject.toSerialized();
    }

    public abstract M register();

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(T t) {
        mSubject.onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        mSubject.onError(e);
    }

    @Override
    public void onComplete() {
        mSubject.onComplete();
    }

}
