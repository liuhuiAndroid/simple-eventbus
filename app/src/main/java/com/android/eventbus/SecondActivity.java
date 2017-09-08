package com.android.eventbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by lh on 2017/9/8.
 */

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
    }

    public void testEventBus(View view) {
        EventBus.getDefault().post(new ToastEvent("测试EventBus SecondActivity"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void toast(ToastEvent toastEvent) {
        Toast.makeText(SecondActivity.this, toastEvent.getContent(), Toast.LENGTH_SHORT).show();
    }

}