package com.joey.keepbook.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.joey.keepbook.R;
import com.joey.keepbook.base.BaseActivity;
import com.joey.keepbook.fragment.BookFragment;
import com.joey.keepbook.fragment.BookInFragment;
import com.joey.keepbook.fragment.BookOutFragment;
import com.joey.keepbook.manager.MyActivityManager;
import com.joey.keepbook.data.Data;
import com.joey.keepbook.listener.ActivityListener;
import com.joey.keepbook.utils.PrefUtils;
import com.joey.keepbook.view.HeadView;

/**
 * Created by Joey on 2016/2/23.
 */
public class BookActivity extends BaseActivity {
    //状态
    private int mIntPage;
    private int mIntState;
    //状态列表
    private int mIntStateIn;
    private int mIntStateOut;
    //key
    private String mStrKeyState;
    //碎片
    private BookFragment mFragment;
    private BookOutFragment bookOutFragment;
    private BookInFragment bookInFragment;
    private FragmentManager fm;
    //view
    private Button btBookOut;
    private Button btBookIn;
    //数据
    private ActivityListener activityListener;
    private Data data;
    private MyActivityManager mActivityManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFinalData();
        initUI();//初始化UI
    }

    /**
     * 初始化最终数据
     */
    private void initFinalData() {
        data = Data.getInstance();
        mActivityManager = MyActivityManager.getInstance();
        //状态列表
        mIntStateIn = mActivityManager.BOOKINFRAGMENT;
        mIntStateOut = mActivityManager.BOOKOUTFRAGMENT;
        //key
        mStrKeyState = mActivityManager.KEY_BOOK_STATE;
        String strKeyPage = mActivityManager.KEY_PAGE;
        //状态
        mIntPage = PrefUtils.getInt(this, strKeyPage, 1);
        mIntState = PrefUtils.getInt(this, mStrKeyState, mIntStateOut);
    }

    /**
     * 初始化数据
     */
    private void initUI() {
        setContentView(R.layout.activity_book);
        btBookOut = (Button) findViewById(R.id.bt_book_out);
        btBookIn = (Button) findViewById(R.id.bt_book_in);
        HeadView hvActivityBook = (HeadView) findViewById(R.id.hv_activity_book);
        //更新Fg
        updateFg();
        /**
         * 设置点击监听
         */
        //支出按钮被点击
        btBookOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntState = mIntStateOut;
                updateFg();
            }
        });

        //收入按钮被点击
        btBookIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntState = mIntStateIn;
                updateFg();
            }
        });

        //标题栏返回被点击
        hvActivityBook.setHeadButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

    private void updateFg() {
        //保存状态
        PrefUtils.putInt(this, mStrKeyState, mIntState);

        /**
         * 按钮状态设置
         * 防止同一个按钮被多次连续点击
         */
        if (mIntState == mIntStateIn) {
            btBookIn.setEnabled(false);
            btBookOut.setEnabled(true);
        } else {
            btBookIn.setEnabled(true);
            btBookOut.setEnabled(false);
        }
        //开启事务
        fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        //移除当前碎片
        if (mFragment != null) {
            transaction.remove(mFragment);
        }

        /**
         * 添加记账碎片
         */
        if (mIntState == mIntStateIn) {
            //添加收入记账碎片
            if (bookInFragment == null) {
                bookInFragment = new BookInFragment();
            }
            mFragment = bookInFragment;
        } else {
            //添加支出记账碎片
            if (bookOutFragment == null) {
                bookOutFragment = new BookOutFragment();
            }
            mFragment = bookOutFragment;
        }
        setActivityListener(mFragment);
        transaction.replace(R.id.fg_book, mFragment);
        transaction.commit();
    }

    /**
     * 释放碎片内存
     */
    private void releaseMemory() {
        /**
         * 释放碎片内存
         */
        if (mFragment == null && !this.isDestroyed()) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.remove(mFragment);
            transaction.commit();
        }
        bookInFragment = null;
        bookOutFragment = null;
        mFragment = null;
        data.close();
        finish();
    }

    @Override
    public void onBackPressed() {
        releaseMemory();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        releaseMemory();
        super.onDestroy();
    }

    public void setActivityListener(ActivityListener activityListener) {
        this.activityListener = activityListener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityListener.onReceiveActivityResult(requestCode, resultCode, data);
    }
}
