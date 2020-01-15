package com.zju.mynotepad.module.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.zju.mynotepad.app.Config;
import com.zju.mynotepad.data.AccountModel;
import com.zju.mynotepad.data.CurveModel;
import com.zju.mynotepad.bean.Notepad;
import com.zju.mynotepad.widget.shape.ShapeResource;

import java.util.List;

import cn.alien95.util.Utils;
import cn.lemon.common.base.presenter.SuperPresenter;


public class MainPresenter extends SuperPresenter<DrawActivity> {

    private Notepad mLocalNotepad;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void saveNote(String title, List<ShapeResource> paths,String url){
        if (TextUtils.isEmpty(title)) {
            Utils.Toast("标题不能为空");
            return;
        }
        Notepad notepad = new Notepad();
        notepad.mTitle = title;
        notepad.mPaths = paths;
        notepad.url = url;
        long time = System.currentTimeMillis();
        notepad.mCreateTime = time;
        notepad.mFileName = time + "";
        AccountModel.getInstance().saveNote(notepad);
        if (mLocalNotepad != null) {
            AccountModel.getInstance().deleteNoteFile(mLocalNotepad.mFileName);
            mLocalNotepad = null;
        }
    }


    public String saveImage(Bitmap bitmap, Context mContext){
        return CurveModel.getInstance().saveCurveToApp(bitmap,mContext);
    }

    public void setLocalNote(Notepad localNotepad){
        mLocalNotepad = localNotepad;
    }

    public void setLocalNoteNull(){
        mLocalNotepad = null;
    }

    public Notepad getLocalNote(){
        return mLocalNotepad;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.NOTE_REQUEST_CODE && resultCode == Config.NOTE_RESULT_CODE) {
            mLocalNotepad = (Notepad) data.getSerializableExtra(Config.NOTE_DATA);
            getView().updateDrawPaths(mLocalNotepad.mPaths);
        }
    }
}
