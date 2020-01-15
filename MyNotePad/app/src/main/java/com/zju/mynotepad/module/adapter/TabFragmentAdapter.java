package com.zju.mynotepad.module.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zju.mynotepad.module.main.FragmentDraw;
import com.zju.mynotepad.module.main.FragmentNote;

public class TabFragmentAdapter extends FragmentPagerAdapter {
    private String [] mTitles;
    public TabFragmentAdapter(FragmentManager fm, String [] mTitles) {
        super(fm);
        this.mTitles=mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        if (position==0){
            return  new FragmentNote();
        }else if (position==1){
            return new FragmentDraw();
        }
        return new FragmentNote();
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }
}