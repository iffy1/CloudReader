package com.example.jingbin.cloudreader.ui;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.app.ConstantsImageUrl;
import com.example.jingbin.cloudreader.base.BaseActivity;
import com.example.jingbin.cloudreader.bean.wanandroid.CoinUserInfoBean;
import com.example.jingbin.cloudreader.data.UserUtil;
import com.example.jingbin.cloudreader.databinding.ActivityMainBinding;
import com.example.jingbin.cloudreader.databinding.NavHeaderMainBinding;
import com.example.jingbin.cloudreader.http.rx.RxBus;
import com.example.jingbin.cloudreader.http.rx.RxBusBaseMessage;
import com.example.jingbin.cloudreader.http.rx.RxCodeConstants;
import com.example.jingbin.cloudreader.ui.film.FilmFragment;
import com.example.jingbin.cloudreader.ui.gank.GankFragment;
import com.example.jingbin.cloudreader.ui.menu.NavAboutActivity;
import com.example.jingbin.cloudreader.ui.menu.NavAdmireActivity;
import com.example.jingbin.cloudreader.ui.menu.NavDeedBackActivity;
import com.example.jingbin.cloudreader.ui.menu.NavDownloadActivity;
import com.example.jingbin.cloudreader.ui.menu.NavHomePageActivity;
import com.example.jingbin.cloudreader.ui.menu.SearchActivity;
import com.example.jingbin.cloudreader.ui.wan.WanFragment;
import com.example.jingbin.cloudreader.ui.wan.child.LoginActivity;
import com.example.jingbin.cloudreader.ui.wan.child.MyCoinActivity;
import com.example.jingbin.cloudreader.ui.wan.child.MyCollectActivity;
import com.example.jingbin.cloudreader.utils.BaseTools;
import com.example.jingbin.cloudreader.utils.CommonUtils;
import com.example.jingbin.cloudreader.utils.DebugUtil;
import com.example.jingbin.cloudreader.utils.DialogBuild;
import com.example.jingbin.cloudreader.utils.GlideUtil;
import com.example.jingbin.cloudreader.utils.PerfectClickListener;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.utils.UpdateUtil;
import com.example.jingbin.cloudreader.view.MyFragmentPagerAdapter;
import com.example.jingbin.cloudreader.view.OnLoginListener;
import com.example.jingbin.cloudreader.view.OnMyPageChangeListener;
import com.example.jingbin.cloudreader.view.statusbar.StatusBarUtil;
import com.example.jingbin.cloudreader.view.webview.WebViewActivity;
import com.example.jingbin.cloudreader.viewmodel.wan.MainViewModel;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by jingbin on 16/11/21.
 *
 * <a href="https://github.com/youlookwhat">Follow me</a>
 * <a href="https://github.com/youlookwhat/CloudReader">source code</a>
 * <a href="http://www.jianshu.com/u/e43c6e979831">Contact me</a>
 */

//MainViewModel iffy 负责navigation页面的内容绑定
//ActivityMainBinding navigationView
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements View.OnClickListener {

    public static boolean isLaunch;
    private ViewPager viewPagerContent;
    private ImageView ivTitleTwo;
    private ImageView ivTitleOne;
    private ImageView ivTitleThree;
    private NavHeaderMainBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //调用baseActivity里面的setContentView, 把baseActivity和MainActivity的layout进行组合
        //activity_main只有navigation的内容
        setContentView(R.layout.activity_main);
        //加载navigationView
        showContentView();

        isLaunch = true;

        //statudsBar toolBar 设置
        initView();

        //初始化三个fragment
        initContentFragment();

        initDrawerLayout();
        initRxBus();
    }

    @Override
    protected void initStatusBar() {
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, bindingView.drawerLayout, CommonUtils.getColor(R.color.colorTheme));
        ViewGroup.LayoutParams layoutParams = bindingView.include.viewStatus.getLayoutParams();
        layoutParams.height = StatusBarUtil.getStatusBarHeight(this);
        bindingView.include.viewStatus.setLayoutParams(layoutParams);
    }

    //statudsBar toolBar 设置
    private void initView() {
        setNoTitle();
        setSupportActionBar(bindingView.include.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }

        //bindingView.include 包含在 android:id="@+id/include"里面的layout="@layout/app_bar_main"
        //Toolbar监听
        viewPagerContent = bindingView.include.vpContent;
        ivTitleOne = bindingView.include.ivTitleOne;
        ivTitleTwo = bindingView.include.ivTitleTwo;
        ivTitleThree = bindingView.include.ivTitleThree;
        bindingView.include.llTitleMenu.setOnClickListener(this);
        bindingView.include.ivTitleOne.setOnClickListener(this);
        bindingView.include.ivTitleTwo.setOnClickListener(this);
        bindingView.include.ivTitleThree.setOnClickListener(this);
        getClipContent();
        UpdateUtil.check(this, false);
    }

    /**
     * inflateHeaderView 进来的布局要宽一些
     */
    private void initDrawerLayout() {
        bindingView.navView.inflateHeaderView(R.layout.nav_header_main);
        View headerView = bindingView.navView.getHeaderView(0);
        bind = DataBindingUtil.bind(headerView);
        bind.setViewModel(viewModel);
        bind.dayNightSwitch.setChecked(SPUtils.getNightMode());
        viewModel.isReadOk.set(SPUtils.isRead());

        GlideUtil.displayCircle(bind.ivAvatar, ConstantsImageUrl.IC_AVATAR);
        //NavigationView 设置回调监听
        bind.llNavExit.setOnClickListener(this);
        bind.ivAvatar.setOnClickListener(this);

        bind.llNavHomepage.setOnClickListener(listener);
        bind.llNavScanDownload.setOnClickListener(listener);
        bind.llNavDeedback.setOnClickListener(listener);
        bind.llNavAbout.setOnClickListener(listener);
        bind.llNavLogin.setOnClickListener(listener);
        bind.llNavCollect.setOnClickListener(listener);
        bind.llInfo.setOnClickListener(listener);
        bind.llNavCoin.setOnClickListener(listener);
        bind.llNavAdmire.setOnClickListener(listener);
        bind.tvRank.setOnClickListener(listener);

        viewModel.getUserInfo();
        viewModel.getCoin().observe(this, new Observer<CoinUserInfoBean>() {
            @Override
            public void onChanged(@Nullable CoinUserInfoBean coinUserInfoBean) {
                if (coinUserInfoBean != null) {
                    bind.tvUsername.setText(coinUserInfoBean.getUsername());
                    bind.tvLevel.setText(String.format("Lv.%s", UserUtil.getLevel(coinUserInfoBean.getCoinCount())));
                    bind.tvRank.setText(String.format("排名 %s", coinUserInfoBean.getRank()));
                } else {
                    bind.tvUsername.setText("玩安卓登录");
                    bind.tvLevel.setText("Lv.1");
                    bind.tvRank.setText("");
                }
            }
        });
    }

    //初始化三个fragment
    private void initContentFragment() {
        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        //每个fragment都含有ViewPager
        mFragmentList.add(new WanFragment());
        mFragmentList.add(new GankFragment());
        mFragmentList.add(new FilmFragment());
        // 注意使用的是：getSupportFragmentManager
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        viewPagerContent.setAdapter(adapter);
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        viewPagerContent.setOffscreenPageLimit(2);
        viewPagerContent.addOnPageChangeListener(new OnMyPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setCurrentItem(0);
                        break;
                    case 1:
                        setCurrentItem(1);
                        break;
                    case 2:
                        setCurrentItem(2);
                        break;
                    default:
                        break;
                }
            }
        });
        setCurrentItem(0);
    }

    //NavigationView的点击回调
    private PerfectClickListener listener = new PerfectClickListener() {
        @Override
        protected void onNoDoubleClick(final View v) {
            bindingView.drawerLayout.closeDrawer(GravityCompat.START);
            bindingView.drawerLayout.postDelayed(() -> {
                switch (v.getId()) {
                    case R.id.ll_nav_homepage:
                        // 主页
                        NavHomePageActivity.startHome(MainActivity.this);
                        break;
                    case R.id.ll_nav_scan_download:
                        //扫码下载
                        NavDownloadActivity.start(MainActivity.this);
                        break;
                    case R.id.ll_nav_deedback:
                        // 问题反馈
                        NavDeedBackActivity.start(MainActivity.this);
                        if (viewModel.isReadOk.get() != null && !viewModel.isReadOk.get().booleanValue()) {
                            SPUtils.setRead(true);
                            viewModel.isReadOk.set(true);
                        }
                        break;
                    case R.id.ll_nav_about:
                        // 关于云阅
                        NavAboutActivity.start(MainActivity.this);
                        break;
                    case R.id.ll_nav_collect:
                        // 玩安卓收藏
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCollectActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_login:
                        // 玩安卓登录
                        DialogBuild.showItems(v, new OnLoginListener() {
                            @Override
                            public void loginWanAndroid() {
                                LoginActivity.start(MainActivity.this);
                            }

                            @Override
                            public void loginGitHub() {
                                WebViewActivity.loadUrl(v.getContext(), "https://github.com/login", "登录GitHub账号");
                            }
                        });
                        break;
                    case R.id.ll_info:
                        // 登录
                        if (!UserUtil.isLogin()) {
                            LoginActivity.start(MainActivity.this);
                        } else {
                            MyCoinActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_coin:
                        // 我的积分
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCoinActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.tv_rank:
                        // 排行
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCoinActivity.startRank(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_admire:
                        // 赞赏
                        NavAdmireActivity.start(MainActivity.this);
                        break;
                    default:
                        break;
                }
            }, 260);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_title_menu:
                // 开启菜单
                bindingView.drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_title_two:
                // 不然cpu会有损耗
                if (viewPagerContent.getCurrentItem() != 1) {
                    setCurrentItem(1);
                }
                break;
            case R.id.iv_title_one:
                if (viewPagerContent.getCurrentItem() != 0) {
                    setCurrentItem(0);
                }
                break;
            case R.id.iv_title_three:
                if (viewPagerContent.getCurrentItem() != 2) {
                    setCurrentItem(2);
                }
                break;
            case R.id.iv_avatar:
                // 头像进入GitHub
                WebViewActivity.loadUrl(v.getContext(), CommonUtils.getString(R.string.string_url_cloudreader), "CloudReader");
                break;
            case R.id.ll_nav_exit:
                // 退出应用
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 切换页面
     *
     * @param position 分类角标
     */
    private void setCurrentItem(int position) {
        boolean isOne = false;
        boolean isTwo = false;
        boolean isThree = false;
        switch (position) {
            case 0:
                isOne = true;
                break;
            case 1:
                isTwo = true;
                break;
            case 2:
                isThree = true;
                break;
            default:
                isTwo = true;
                break;
        }
        viewPagerContent.setCurrentItem(position);
        ivTitleOne.setSelected(isOne);
        ivTitleTwo.setSelected(isTwo);
        ivTitleThree.setSelected(isThree);
    }

    /**
     * 夜间模式待完善
     */
    public boolean getNightMode() {
        return SPUtils.getNightMode();
    }

    public void onNightModeClick(View view) {
        if (!SPUtils.getNightMode()) {
//            SkinCompatManager.getInstance().loadSkin(Constants.NIGHT_SKIN);
        } else {
            // 恢复应用默认皮肤
//            SkinCompatManager.getInstance().restoreDefaultTheme();
        }
        SPUtils.setNightMode(!SPUtils.getNightMode());
        bind.dayNightSwitch.setChecked(SPUtils.getNightMode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            SearchActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //iffy 这段代码不会走到，在@onKeyDonw()里已经处理了back按钮
    @Override
    public void onBackPressed() {
        DebugUtil.debug(MainActivity.class.getSimpleName()+"onBackPressed（） 这段代码不需要，在onkeydonw里已经处理了back按钮 ");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 获取剪切板链接
     */
    private void getClipContent() {
        String clipContent = BaseTools.getClipContent();
        if (!TextUtils.isEmpty(clipContent)) {
            DialogBuild.showCustom(viewPagerContent, clipContent, "打开其中链接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WebViewActivity.loadUrl(MainActivity.this, clipContent, "加载中..");
                    BaseTools.clearClipboard();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DebugUtil.debug(MainActivity.class.getSimpleName()+"onKeyDown（） keyCode == KeyEvent.KEYCODE_BACK ");
            if (bindingView.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                bindingView.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                // 不退出程序，进入后台
                //iffy 这里把back的行为改为home键行为，activity不会销毁,这样下次再点击launch icon就可以直接恢复这个界面，不会再显示广告
                //改写返回按键，不销毁Activity https://blog.csdn.net/qq_29543611/article/details/80219670
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 每日推荐点击"新电影热映榜"跳转
     */
    private void initRxBus() {
        Disposable subscribe = RxBus.getDefault().toObservable(RxCodeConstants.JUMP_TYPE_TO_ONE, RxBusBaseMessage.class)
                .subscribe(new Consumer<RxBusBaseMessage>() {
                    @Override
                    public void accept(RxBusBaseMessage rxBusBaseMessage) throws Exception {
                        setCurrentItem(2);
                    }
                });
        addSubscription(subscribe);
        Disposable subscribe2 = RxBus.getDefault().toObservable(RxCodeConstants.LOGIN, Boolean.class)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isLogin) throws Exception {
                        if (isLogin) {
                            viewModel.getUserInfo();
                        } else {
                            viewModel.getCoin().setValue(null);
                        }
                    }
                });
        addSubscription(subscribe2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DebugUtil.debug(MainActivity.class.getSimpleName()+"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugUtil.debug(MainActivity.class.getSimpleName()+"onStop");
    }

    @Override
    public void onDestroy() {
        DebugUtil.debug(MainActivity.class.getSimpleName()+"onDestroy");
        super.onDestroy();
        isLaunch = false;
        // 杀死该应用进程 需要权限
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
