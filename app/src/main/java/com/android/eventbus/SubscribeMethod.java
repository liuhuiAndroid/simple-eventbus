package com.android.eventbus;

import java.lang.reflect.Method;

/**
 * Created by lh on 2017/9/7.
 * 这个里面存储了我们需要运行方法的所有参数，毕竟我们运行时，需要该方法，该方法所在的对象，以及在什么线程运行；
 */
public class SubscribeMethod {

    Method method;
    ThreadMode threadMode;
    Object subscriber;

    public SubscribeMethod(Method method, ThreadMode threadMode, Object subscriber) {
        this.method = method;
        this.threadMode = threadMode;
        this.subscriber = subscriber;
    }
}
