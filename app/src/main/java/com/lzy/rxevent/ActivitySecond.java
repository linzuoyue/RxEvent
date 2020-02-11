package com.lzy.rxevent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lzy.lib.rxevent.ActivityRx;
import com.lzy.lib.rxevent.BaseEventObserver;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ActivitySecond extends ActivityRx {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        findViewById(R.id.btn_send_event).setOnClickListener(view -> {
            Schedulers.io().scheduleDirect(() -> {
                for (int i = 0; i < 10; i++) {
                    TestEventManager.getInstance().onNext(new TestEventManager.TestEvent(TestEventManager.TestEvent.EventType.EVENT_1));
                    TestEventManager.getInstance().onNext(new TestEventManager.TestEvent(TestEventManager.TestEvent.EventType.EVENT_2));
                }
            });
        });

        TestEventManager.getInstance().register().lifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(new BaseEventObserver<TestEventManager.TestEvent>() {

                    private int count = 0;

                    @Override
                    protected void onResponse(boolean isSuccess, TestEventManager.TestEvent testEvent) {
                        if (!isSuccess || testEvent == null) {
                            return;
                        }
                        count++;
                        String msg = "event total counts:" + count;
                        Log.e("Second", msg);
                    }
                });

    }
}
