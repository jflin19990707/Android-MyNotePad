package com.zju.mynotepad.module.main;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.tabs.TabLayout;
import com.zju.mynotepad.R;
import com.zju.mynotepad.db.NoteDao;
import com.zju.mynotepad.bean.Notepad;
import com.zju.mynotepad.module.adapter.NotepadAdapter;
import com.zju.mynotepad.module.adapter.TabFragmentAdapter;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static NotepadAdapter mAdapter;
    public TabLayout mTablayout;
    public ViewPager mViewpager;
    private TabFragmentAdapter adapter;
    private TextView tabText_note;
    private TextView tabText_graph;

    public static NoteDao noteDao;

    public static int widthPixels;
    public static int heightPixels;

    String [] titles={"笔记","画图"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new NotepadAdapter(this);
        Fresco.initialize(MainActivity.this);
        setContentView(R.layout.activity_main);
        mTablayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewpager= (ViewPager) findViewById(R.id.fragment_pager);

        //初始化ViewPager
        initViewPager();

        //初始化TabLayout
        initTabLayout();

        noteDao = new NoteDao(this);


        //获取手机屏幕的宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        widthPixels = outMetrics.widthPixels;
        heightPixels = outMetrics.heightPixels;
    }

    public static NotepadAdapter getmAdapter(){
        return mAdapter;
    }

    public void setData(List<Notepad> data){
        mAdapter.clear();
        mAdapter.addAll(data);

    }

    public void initViewPager() {
        adapter=new TabFragmentAdapter(getSupportFragmentManager(),titles);
        mViewpager.setAdapter(adapter);
        mViewpager.setCurrentItem(0);
        //ViewPager点击事件
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //选中ViewPager与TabLayout联动
                mTablayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void initTabLayout(){
        mTablayout.setTabMode(TabLayout.MODE_FIXED);//模式，可滑动，默认是不可滑动
        mTablayout.setSelectedTabIndicatorColor(Color.parseColor("#00000000"));//指示器颜色
        mTablayout.addTab(mTablayout.newTab().setCustomView(R.layout.tabview_note));
        tabText_note= (TextView) findViewById(R.id.tv_note);

        mTablayout.addTab(mTablayout.newTab().setCustomView(R.layout.tabview_graph));
        tabText_graph= (TextView) findViewById(R.id.tv_graph);

        //TabLayout点击事件
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            //选中tab
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //选中tab时,TabLayout与ViewPager联动
                mViewpager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        tabText_note.setTextColor(getResources().getColor(R.color.md_yellow_800_color_code));
                        break;
                    case 1:
                        tabText_graph.setTextColor(getResources().getColor(R.color.md_yellow_800_color_code));
                        break;
                }
            }
            //未选择tab
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        tabText_note.setTextColor(getResources().getColor(R.color.md_black_color_code));
                        break;
                    case 1:
                        tabText_graph.setTextColor(getResources().getColor(R.color.md_black_color_code));
                        break;
                }
            }
            //重复选中tab
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

}