package com.lzy.lib.rxevent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * desc: 事件基类 <br/>
 * author: 林佐跃 <br/>
 * date: 2018/8/15 <br/>
 * since V mello 1.0.0 <br/>
 */
public class BaseEvent<E>{

    /**
     * 类型
     */
    @NonNull
    private Integer type;

    /**
     * 子类型
     */
    @Nullable
    private Integer subType;

    /**
     * 额外
     */
    @Nullable
    private E extra;

    public BaseEvent(@NonNull Integer type) {
        this(type, null);

    }

    public BaseEvent(@NonNull Integer type, @Nullable Integer subType) {
        this(type, subType, null);
    }

    public BaseEvent(@NonNull Integer type, @Nullable Integer subType, @Nullable E extra) {
        this.type = type;
        this.subType = subType;
        this.extra = extra;
    }

    @NonNull
    public Integer getType() {
        return type;
    }

    public void setType(@NonNull Integer type) {
        this.type = type;
    }

    @Nullable
    public Integer getSubType() {
        return subType;
    }

    public void setSubType(@Nullable Integer subType) {
        this.subType = subType;
    }

    @Nullable
    public E getExtra() {
        return extra;
    }

    public void setExtra(@Nullable E extra) {
        this.extra = extra;
    }



}
