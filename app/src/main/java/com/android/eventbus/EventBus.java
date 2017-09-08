package com.android.eventbus;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lh on 2017/9/7.
 */

public class EventBus {

    static volatile EventBus defaultInstance;
    private Handler mHandler;

    /*
     * 我们的强大的map，存储我们的方法
     * 使用了一个Map存储所有的方法，key为参数的类型class；value为CopyOnWriteArrayList<SubscribeMethod>
     * CopyOnWriteArrayList是什么东西？
     */
    private final Map<Class<?>, CopyOnWriteArrayList<SubscribeMethod>> subscriptionsByEventType;

    public EventBus() {
        subscriptionsByEventType = new HashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 使用双重检查锁定实现的懒汉式单例类
     *
     * @return
     */
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }

    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        Method[] methods = subscriberClass.getDeclaredMethods();
        CopyOnWriteArrayList<SubscribeMethod> subscribeMethods = null;

        // 遍历该类的所有方法
        for (Method method : methods) {
            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
            // 判断方法是否有Subscribe注解
            if (subscribeAnnotation != null) {// Subscribe.class
                SubscribeMethod subscribeMethod = null;
                Class<?>[] parameterTypes = method.getParameterTypes();
                // 参数的个数为1
                if (parameterTypes.length == 1) {
                    Class<?> type = parameterTypes[0];
                    synchronized (this) {
                        if (subscriptionsByEventType.containsKey(type)) {
                            subscribeMethods = subscriptionsByEventType.get(type);
                        } else {
                            subscribeMethods = new CopyOnWriteArrayList<>();
                            subscriptionsByEventType.put(type, subscribeMethods);
                        }
                    }

                    // 方法在什么线程运行。默认在当前
                    ThreadMode threadMode = subscribeAnnotation.threadMode();
                    // 提取出method，mode，方法所在类对象，存数的类型封装成为SubscribeMethod
                    subscribeMethod = new SubscribeMethod(method, threadMode, subscriber);
                    // 存在Map里面
                    subscribeMethods.add(subscribeMethod);
                }
            }
        }
    }

    public void post(Object eventType) {
        // 拿到该线程中的PostingThread对象
        PostingThreadState postingThreadState = mPostingThread.get();
        postingThreadState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
        // 将事件加入事件队列
        List<Object> eventQueue = postingThreadState.eventQueue;
        eventQueue.add(eventType);
        // 防止多次调用
        if (postingThreadState.isPosting) {
            return;
        }
        postingThreadState.isPosting = true;
        // 取出所有事件进行调用
        try {
            while (!eventQueue.isEmpty()) {
                postEvent(eventQueue.remove(0), postingThreadState);
            }
        } finally {
            postingThreadState.isPosting = false;
            postingThreadState.isMainThread = false;
        }
    }

    /**
     * 最终发布的事件先加入到事件队列，然后再取出来调用postEvent
     *
     * @param eventType
     * @param postingThreadState
     */
    private void postEvent(final Object eventType, PostingThreadState postingThreadState) {
        CopyOnWriteArrayList<SubscribeMethod> subscribeMethods;
        synchronized (this) {
            // 直接根据参数类型，去map改到该方法
            subscribeMethods = subscriptionsByEventType.get(eventType.getClass());
        }
        if (subscribeMethods != null) {
            for (final SubscribeMethod subscribeMethod : subscribeMethods) {
                // 根据其threadMode，如果在UI线程，则判断当前线程，如果是UI线程，直接调用，否则通过handler执行；
                switch (subscribeMethod.threadMode) {

                    case POSTING:
                        invokeMethod(eventType, subscribeMethod);
                        break;

                    case MAIN:
                        if (postingThreadState.isMainThread) {
                            invokeMethod(eventType, subscribeMethod);
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    invokeMethod(eventType, subscribeMethod);
                                }
                            });
                        }
                        break;

                    case BACKGROUND:
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                invokeMethod(eventType, subscribeMethod);
                                return null;
                            }
                        };
                        break;

                    case ASYNC:
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                invokeMethod(eventType, subscribeMethod);
                                return null;
                            }
                        };
                        break;

                    default:
                        throw new IllegalStateException("Unknown thread mode: " + subscribeMethod.threadMode);
                }
            }
        }
    }

    /**
     * 反射调用方法
     */
    private void invokeMethod(Object eventType, SubscribeMethod subscribeMethod) {
        try {
            subscribeMethod.method.invoke(subscribeMethod.subscriber, eventType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unregister(Object subscriber) {
        Class<?> clazz = subscriber.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
            if (subscribeAnnotation != null) {// Subscribe.class
                Class<?>[] parameterTypes = method.getParameterTypes();
                synchronized (this) {
                    CopyOnWriteArrayList<SubscribeMethod> subscribeMethods = subscriptionsByEventType.get(parameterTypes[0]);
                    CopyOnWriteArrayList<SubscribeMethod> subscribeMethodsCope = new CopyOnWriteArrayList<>(subscribeMethods);
                    for (SubscribeMethod subscribeMethod : subscribeMethodsCope) {
                        if(subscribeMethod.subscriber.getClass().equals(subscriber.getClass())){
                            subscribeMethods.remove(subscribeMethod);
                        }
                    }
                }
            }
        }
    }

    final static class PostingThreadState {
        final List<Object> eventQueue = new ArrayList<Object>();
        boolean isPosting;
        boolean isMainThread;
    }

    private ThreadLocal<PostingThreadState> mPostingThread = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

}
