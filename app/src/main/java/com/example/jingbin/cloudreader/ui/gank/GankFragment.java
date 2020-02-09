package com.example.jingbin.cloudreader.ui.gank;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.base.BaseFragment;
import com.example.jingbin.cloudreader.databinding.FragmentContentBinding;
import com.example.jingbin.cloudreader.http.rx.RxBus;
import com.example.jingbin.cloudreader.http.rx.RxCodeConstants;
import com.example.jingbin.cloudreader.ui.gank.child.AndroidFragment;
import com.example.jingbin.cloudreader.ui.gank.child.CustomFragment;
import com.example.jingbin.cloudreader.ui.gank.child.EverydayFragment;
import com.example.jingbin.cloudreader.ui.gank.child.WelfareFragment;
import com.example.jingbin.cloudreader.view.MyFragmentPagerAdapter;
import com.example.jingbin.cloudreader.viewmodel.menu.NoViewModel;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by jingbin on 16/11/21.
 * 展示干货的页面
 */
public class GankFragment extends BaseFragment<NoViewModel, FragmentContentBinding> {

    private ArrayList<String> mTitleList = new ArrayList<>(4);
    private ArrayList<Fragment> mFragments = new ArrayList<>(4);
    private boolean mIsFirst = true;
    private boolean mIsPrepared;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mIsPrepared = true;
    }

    //setUserVisibleHint（）fragment可见的回调
    @Override
    protected void loadData() {
        if (!mIsPrepared || !mIsVisible || !mIsFirst) {
            return;
        }
        //首先在主线程的Handler中就已经post进去了一个Runnable来执行performTraversals方法，
        // 当然就按照顺序执行了post我们调用View.post(runnable)方法到主线程Handler、测量、布局、绘制等一系列操作。
        // 然而由于Handler的机制，它是将所有的message都post到一个MessageQueue中，
        // 按照顺序执行这些消息。因此只有当执行完测量、布局、绘制之后，才能执行我们的Runnable，
        // 所以我们这时就能够获取到正确的宽高了~
        //使用postDelayed方法就是为了等viewPager准备好后再去写内容

        bindingView.vpGank.postDelayed(() -> {
            showContentView();
            //实例化fragment
            initFragmentList();
            MyFragmentPagerAdapter myAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), mFragments, mTitleList);
            bindingView.vpGank.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
            // 左右预加载页面的个数
            bindingView.vpGank.setOffscreenPageLimit(3);
            //绑定tabView和viewpager
            bindingView.tabGank.setupWithViewPager(bindingView.vpGank);
            // item点击跳转
            initRxBus();
            mIsFirst = false;
        }, 120);
    }

    @Override
    public int getContent() {
        return R.layout.fragment_content;
    }

    //新建需要的Fragment
    private void initFragmentList() {
        mTitleList.clear();
        mTitleList.add("每日推荐");
        mTitleList.add("福利");
        mTitleList.add("干货订制");
        mTitleList.add("大安卓");
        mFragments.add(new EverydayFragment());
        mFragments.add(new WelfareFragment());
        mFragments.add(new CustomFragment());
        mFragments.add(AndroidFragment.newInstance("Android"));
    }

    /**
     * 每日推荐点击"更多"跳转
     */
    private void initRxBus() {
        Disposable subscribe = RxBus.getDefault().toObservable(RxCodeConstants.JUMP_TYPE, Integer.class)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == 0) {
                            bindingView.vpGank.setCurrentItem(3);
                        } else if (integer == 1) {
                            bindingView.vpGank.setCurrentItem(1);
                        } else if (integer == 2) {
                            bindingView.vpGank.setCurrentItem(2);
                        }
                    }
                });
        addSubscription(subscribe);
    }
}
