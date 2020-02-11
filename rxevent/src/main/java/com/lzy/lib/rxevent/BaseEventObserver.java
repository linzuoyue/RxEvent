package com.lzy.lib.rxevent;


import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * desc: Observer基类 <br/>
 * time: 2017/9/7 15:48 <br/>
 * author: Vincent <br/>
 * since V1.0 <br/>
 */
public abstract class BaseEventObserver<T> implements Observer<T> {
	private Disposable mDisposable;

	protected abstract void onResponse(boolean isSuccess, T t);

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		mDisposable = d;
	}

	@Override
	public void onNext(@NonNull T t) {
		//如果不是接口请求响应
		onResponse(true, t);
	}

	@Override
	public void onError(@NonNull Throwable e) {
		onResponse(false, null);
	}

	public void disposed() {
		if (mDisposable != null && !mDisposable.isDisposed()) {
			mDisposable.dispose();
		}
	}

	@Override
	public void onComplete() {
		disposed();
	}

	public boolean isDisposed() {
		return mDisposable == null || mDisposable.isDisposed();
	}

}