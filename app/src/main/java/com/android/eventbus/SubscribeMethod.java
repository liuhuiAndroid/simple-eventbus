package com.android.eventbus;

import java.lang.reflect.Method;

/**
 * Created by lh on 2017/9/7.
 */

public class SubscribeMethod {

    Method method;
    Object subscriber;

    public SubscribeMethod(Method method,Object subscriber) {
        this.method = method;
        this.subscriber = subscriber;
    }


}
