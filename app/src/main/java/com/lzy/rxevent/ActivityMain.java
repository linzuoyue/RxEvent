package com.lzy.rxevent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lzy.lib.rxevent.ActivityRx;
import com.lzy.lib.rxevent.BaseEventObserver;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ActivityMain extends ActivityRx {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvTotal = findViewById(R.id.tv_event_total_count);
        final TextView tv1 = findViewById(R.id.tv_event_1_count);
        final TextView tv2 = findViewById(R.id.tv_event_2_count);

        findViewById(R.id.btn_send_event).setOnClickListener(view -> {
            Schedulers.io().scheduleDirect(() -> {
                TestEventManager.getInstance().onNext(new TestEventManager.TestEvent(TestEventManager.TestEvent.EventType.EVENT_1));
                TestEventManager.getInstance().onNext(new TestEventManager.TestEvent(TestEventManager.TestEvent.EventType.EVENT_2));
            });
        });

        findViewById(R.id.btn_second_activity).setOnClickListener(view -> {
            Intent intent = new Intent(this, ActivitySecond.class);
            startActivity(intent);
        });

        TestEventManager.getInstance().register().lifecyclePack(this)
                .observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(new BaseEventObserver<List<TestEventManager.TestEvent>>() {

                    private int count = 0;

                    @Override
                    protected void onResponse(boolean isSuccess, List<TestEventManager.TestEvent> testEvents) {
                        if (!isSuccess || testEvents == null || testEvents.isEmpty()) {
                            return;
                        }
                        count += testEvents.size();
                        String msg = "event total counts:" + count;
                        Log.e("main", msg);
                        tvTotal.setText(msg);
                    }
                });

        TestEventManager.getInstance().register()
                .filterEventType(TestEventManager.TestEvent.EventType.EVENT_1)
                .lifecyclePack(this)
                .observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(new BaseEventObserver<List<TestEventManager.TestEvent>>() {

                    private int count = 0;

                    @Override
                    protected void onResponse(boolean isSuccess, List<TestEventManager.TestEvent> testEvents) {
                        if (!isSuccess || testEvents == null || testEvents.isEmpty()) {
                            return;
                        }
                        count += testEvents.size();
                        String msg = "event 1 counts:" + count;
                        Log.e("main", msg);
                        tv1.setText(msg);
                    }
                });
        TestEventManager.getInstance().register()
                .filterEventType(TestEventManager.TestEvent.EventType.EVENT_2)
                .filterLifecycleResume(this)
                .recycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .safeSubscribe(new BaseEventObserver<TestEventManager.TestEvent>() {

                    private int count = 0;

                    @Override
                    protected void onResponse(boolean isSuccess, TestEventManager.TestEvent testEvent) {
                        if (!isSuccess || testEvent == null) {
                            return;
                        }
                        count++;
                        String msg = "event 2 counts:" + count;
                        Log.e("main", msg);
                        tv2.setText(msg);
                    }
                });


    }
}
