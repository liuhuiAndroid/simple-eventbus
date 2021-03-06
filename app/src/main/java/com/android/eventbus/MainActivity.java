package com.android.eventbus;

import android.content.Intent;
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

    public void testEventBus(View view) {
        EventBus.getDefault().post(new ToastEvent("测试EventBus MainActivity"));
    }

    public void testJump(View view) {
        startActivity(new Intent(MainActivity.this, SecondActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void toast(ToastEvent toastEvent) {
        Toast.makeText(MainActivity.this, toastEvent.getContent(), Toast.LENGTH_SHORT).show();
    }

}