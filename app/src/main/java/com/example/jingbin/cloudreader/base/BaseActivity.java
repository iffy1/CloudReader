package com.example.jingbin.cloudreader.base;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.databinding.ActivityBaseBinding;
import com.example.jingbin.cloudreader.utils.ClassUtil;
import com.example.jingbin.cloudreader.utils.CommonUtils;
import com.example.jingbin.cloudreader.utils.DebugUtil;
import com.example.jingbin.cloudreader.view.statusbar.StatusBarUtil;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * @author jingbin
 * @date 16/12/10
 */
public abstract class BaseActivity<VM extends AndroidViewModel, SV extends ViewDataBinding> extends AppCompatActivity {

    // ViewModel
    protected VM viewModel;
    // 布局view
    protected SV bindingView;
    private View errorView;
    private View loadingView;
    private ActivityBaseBinding mBaseBinding;
    //加载中（音符）帧动画
    private AnimationDrawable mAnimationDrawable;
    private CompositeDisposable mCompositeDisposable;

    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        DebugUtil.debug(BaseActivity.class.getSimpleName()+"LayoutInflater.from(this)"+LayoutInflater.from(this));
        DebugUtil.debug(BaseActivity.class.getSimpleName()+"getLayoutInflater():"+getLayoutInflater());
        //两种方法获取的layoutInflater是一样的
        //2020-02-06 14:31:59.144 2269-2269/com.example.jingbin.cloudreader D/iffy: BaseActivityLayoutInflater.from(this)com.android.internal.policy.PhoneLayoutInflater@df3d846
        //2020-02-06 14:31:59.144 2269-2269/com.example.jingbin.cloudreader D/iffy: BaseActivitygetLayoutInflater():com.android.internal.policy.PhoneLayoutInflater@df3d846
        mBaseBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_base, null, false);
        //对应activity的Layout
        bindingView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);

        // content
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingView.getRoot().setLayoutParams(params);
        RelativeLayout mContainer = (RelativeLayout) mBaseBinding.getRoot().findViewById(R.id.container);
        //把子view（layoutResID）放入到baseView的container里面
        mContainer.addView(bindingView.getRoot());
        //把bseview设置成content
        getWindow().setContentView(mBaseBinding.getRoot());

        //View inflate()
        //ViewStub 是一个不可见，size为0的View，它通常用于在适当的时机去懒加载布局。
        //ViewStub 的布局在加载后会直接替换它自己，所以 ViewStub 存在于 View hierarchy 直到它调用了 setVisiblity() 或者 inflate()。
        loadingView = ((ViewStub) findViewById(R.id.vs_loading)).inflate();
        ImageView img = loadingView.findViewById(R.id.img_progress);

        // 加载帧动画
        mAnimationDrawable = (AnimationDrawable) img.getDrawable();
        // 默认进入页面就开启动画
        if (!mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }

        //iffy 注释后没看到影响
        setToolBar(mBaseBinding.toolBar);

        bindingView.getRoot().setVisibility(View.GONE);

        // 设置透明状态栏，兼容4.4
        initStatusBar();

        //对于mainActivity来说 初始化的是navigation drawer里面的vm
        initViewModel();
    }

    protected void initStatusBar() {
        // 设置透明状态栏，兼容4.4
        StatusBarUtil.setColor(this, CommonUtils.getColor(R.color.colorTheme), 0);
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        DebugUtil.debug("initViewModel() Class:"+this.getClass().getSimpleName());
        Class<VM> viewModelClass = ClassUtil.getViewModel(this);
        DebugUtil.debug("获取viewModel:"+viewModelClass.getSimpleName());
        if (viewModelClass != null) {
            this.viewModel = ViewModelProviders.of(this).get(viewModelClass);
        }
    }

    /**
     * 设置titlebar
     * iffy没看到实际效果
     */
    protected void setToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });
    }

    @Override
    public void setTitle(CharSequence text) {
        mBaseBinding.toolBar.setTitle(text);
    }

    public void setNoTitle() {
        mBaseBinding.toolBar.setVisibility(View.GONE);
    }

    protected void showLoading() {
        if (loadingView != null && loadingView.getVisibility() != View.VISIBLE) {
            loadingView.setVisibility(View.VISIBLE);
        }
        // 开始动画
        if (!mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }

    protected void showContentView() {
        if (loadingView != null && loadingView.getVisibility() != View.GONE) {
            loadingView.setVisibility(View.GONE);
        }
        // 停止动画
        if (mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        if (bindingView.getRoot().getVisibility() != View.VISIBLE) {
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }

    protected void showError() {
        if (loadingView != null && loadingView.getVisibility() != View.GONE) {
            loadingView.setVisibility(View.GONE);
        }
        // 停止动画
        if (mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
        //ViewStub 是XML里面的懒加载机制，必须使用inflate()才能让他显示
        if (errorView == null) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.vs_error_refresh);
            errorView = viewStub.inflate();
            // 点击加载失败布局
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoading();
                    onRefresh();
                }
            });
        } else {
            errorView.setVisibility(View.VISIBLE);
        }
        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
    }

    /**
     * 失败后点击刷新
     */
    protected void onRefresh() {

    }

    public void addSubscription(Disposable s) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }
        this.mCompositeDisposable.add(s);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            getResources();
        }
    }

    /**
     * 禁止改变字体大小 字号不随系统改变
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            // clear 和 dispose的区别是：  disposed = true;
            this.mCompositeDisposable.clear();
        }
    }

    public void removeDisposable() {
        if (this.mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            this.mCompositeDisposable.dispose();
        }
    }
}
