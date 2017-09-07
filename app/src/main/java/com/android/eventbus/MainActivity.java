package com.android.eventbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
    }

    public void testEventBus(View view){
        EventBus.getDefault().post(new ToastEvent("测试EventBus"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void toast(ToastEvent toastEvent) {
        Toast.makeText(MainActivity.this, toastEvent.getContent(), Toast.LENGTH_SHORT).show();
    }

}

class ToastEvent {
    String content;

    public ToastEvent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
