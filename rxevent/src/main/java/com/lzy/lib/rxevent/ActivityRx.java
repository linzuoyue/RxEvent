package com.lzy.lib.rxevent;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * desc: RxActivity <br/>
 * time: 2017/9/11 18:00 <br/>
 * author: Vincent <br/>
 * since V1.0 <br/>
 */
@SuppressWarnings("ResourceType")
public abstract class ActivityRx extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {

	public final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

	@Override
	@NonNull
	@CheckResult
	public final Observable<ActivityEvent> lifecycle() {
		return lifecycleSubject.hide();
	}

	@Override
	@NonNull
	@CheckResult
	public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
		return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
	}

	@Override
	@NonNull
	@CheckResult
	public final <T> LifecycleTransformer<T> bindToLifecycle() {
		return RxLifecycleAndroid.bindActivity(lifecycleSubject);
	}

	@Override
	@CallSuper
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lifecycleSubject.onNext(ActivityEvent.CREATE);
	}

	@Override
	@CallSuper
	protected void onStart() {
		super.onStart();
		lifecycleSubject.onNext(ActivityEvent.START);
	}

	@Override
	@CallSuper
	protected void onResume() {
		super.onResume();
		lifecycleSubject.onNext(ActivityEvent.RESUME);
	}

	@Override
	@CallSuper
	protected void onPause() {
		lifecycleSubject.onNext(ActivityEvent.PAUSE);
		super.onPause();
	}

	@Override
	@CallSuper
	protected void onStop() {
		lifecycleSubject.onNext(ActivityEvent.STOP);
		super.onStop();
	}

	@Override
	@CallSuper
	protected void onDestroy() {
		lifecycleSubject.onNext(ActivityEvent.DESTROY);
		super.onDestroy();
	}

}
