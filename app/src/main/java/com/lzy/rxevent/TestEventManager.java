package com.lzy.rxevent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lzy.lib.rxevent.BaseEvent;
import com.lzy.lib.rxevent.BaseEventManager;
import com.lzy.lib.rxevent.BaseObservableWrap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

public class TestEventManager extends BaseEventManager<TestEventManager.TestEvent, TestEventManager.ObservableWrap> {

    public static TestEventManager getInstance() {
        return TestEventManager.InstanceHolder.instance;
    }

    @Override
    public ObservableWrap register() {
        return new ObservableWrap(mSubject.ofType(TestEventManager.TestEvent.class));
    }

    public static class ObservableWrap extends BaseObservableWrap<TestEvent, ObservableWrap> {



        ObservableWrap(@NonNull ObservableSource<TestEventManager.TestEvent> source) {
            super(source);
        }

        @Override
        protected ObservableWrap wrap(@NonNull Observable<TestEventManager.TestEvent> observable) {
            return new ObservableWrap(observable);
        }

        /**
         * 筛选事件
         *
         * @param eventTypes 事件类型
         */
        public ObservableWrap filterEventType(@TestEventManager.TestEvent.EventType int ... eventTypes) {
            return wrap(filter(videoEvent -> {
                for (int eventType : eventTypes) {
                    if (videoEvent.getType() == eventType) {
                        return true;
                    }
                }
                return false;
            }));
        }

    }

    public static class TestEvent<E> extends BaseEvent<E> {

        public TestEvent(@EventType int eventType, @Nullable E extra) {
            super(eventType, null, extra);
        }

        public TestEvent(@EventType int eventType) {
            super(eventType);
        }

        @IntDef({EventType.EVENT_1, EventType.EVENT_2})
        @Retention(RetentionPolicy.SOURCE)
        public @interface EventType {

            // 事件1
            int EVENT_1 = 0;
            // 事件2
            int EVENT_2 = 1;
        }

    }

    private static class InstanceHolder {

        private static TestEventManager instance = new TestEventManager();

    }

}
