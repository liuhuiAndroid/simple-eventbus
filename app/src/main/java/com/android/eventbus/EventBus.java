package com.android.eventbus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lh on 2017/9/7.
 */

public class EventBus {

    static volatile EventBus defaultInstance;


    private final Map<Class<?>, CopyOnWriteArrayList<SubscribeMethod>> subscriptionsByEventType;

    public EventBus() {
        subscriptionsByEventType = new HashMap<>();
    }

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

        for (Method method : methods) {
            if (method.getName().startsWith("toast")) {
                SubscribeMethod subscribeMethod = null;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Class<?> type = parameterTypes[0];

                    if (subscriptionsByEventType.containsKey(type)) {
                        subscribeMethods = subscriptionsByEventType.get(type);
                    } else {
                        subscribeMethods = new CopyOnWriteArrayList<>();
                        subscriptionsByEventType.put(type, subscribeMethods);
                    }

                    subscribeMethod = new SubscribeMethod(method, subscriber);
                    // 存在Map里面
                    subscribeMethods.add(subscribeMethod);
                }
            }

        }
    }

    public void post(Object eventType) {
        postEvent(eventType);
    }

    private void postEvent(Object eventType) {
        CopyOnWriteArrayList<SubscribeMethod> subscribeMethods = subscriptionsByEventType.get(eventType.getClass());

        for (final SubscribeMethod subscribeMethod : subscribeMethods) {
            invokeMethod(eventType, subscribeMethod);
        }
    }

    private void invokeMethod(Object eventType, SubscribeMethod subscribeMethod) {
        try {
            subscribeMethod.method.invoke(subscribeMethod.subscriber, eventType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void unregister(Object subscriber) {
        subscriptionsByEventType.clear();
    }
}
