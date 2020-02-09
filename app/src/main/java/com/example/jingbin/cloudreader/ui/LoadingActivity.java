package com.example.jingbin.cloudreader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.utils.DebugUtil;

/**
 * 解决启动白屏问题
 *
 * @author jingbin
 */

//Slash界面
public class LoadingActivity extends FragmentActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 后台返回时可能启动这个页面 http://blog.csdn.net/jianiuqi/article/details/54091181
        //Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT 转换为2金之后为 10000000000000000000000
        DebugUtil.debug(LoadingActivity.class.getSimpleName()+"getIntent().getFlags():"+getIntent().getFlags());
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            DebugUtil.debug(LoadingActivity.class.getSimpleName()+"finish");
            finish();
            return;
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                //那么怎么才能让Acitivity的切换更优雅呢，Android中提供了一个方法来解决这个问题，
                // 即overridePendingTransition(A，B)函数。
                //B->A 新的Activity进入时的动画 -> 旧的Activity出去时的动画
                overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
                finish();
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        DebugUtil.debug(LoadingActivity.class.getSimpleName()+"onDestroy");
        //handler.removeCallbacksAndMessages(null);
        handler = null;
        super.onDestroy();
    }
}
