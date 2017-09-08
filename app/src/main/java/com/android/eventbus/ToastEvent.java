package com.android.eventbus;

/**
 * Created by lh on 2017/9/8.
 */

public class ToastEvent {

    String content;

    public ToastEvent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
