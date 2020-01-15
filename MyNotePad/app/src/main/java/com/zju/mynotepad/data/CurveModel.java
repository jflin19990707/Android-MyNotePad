package com.zju.mynotepad.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cn.alien95.util.AsyncThreadPool;
import cn.alien95.util.Utils;
import cn.lemon.common.base.model.SuperModel;


public class CurveModel extends SuperModel {

    private File mAppRootDir;
    private Handler mHandler;
    private String url;

    public CurveModel() {
        mAppRootDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!mAppRootDir.exists()) {
            mAppRootDir.mkdir();
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static CurveModel getInstance() {
        return getInstance(CurveModel.class);
    }

    //保存到APP目录
    public String saveCurveToApp(final Bitmap resource,Context mContext) {
//        File image = new File(mAppRootDir, System.currentTimeMillis() + ".png");
        return saveImage(resource, mContext);
    }

    private static String generateFileName(){
        return UUID.randomUUID().toString();
    }

    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/OA头像/";

    private String saveImage(final Bitmap resource, Context context) {
        if (resource.getByteCount() == 0) {
            Utils.Toast("bitmap is empty");
            return null;
        }
        url = "";
        String savePath;
        String fileName = generateFileName() + ".JPEG";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
            return null;
        }
        File filePic = new File(savePath + fileName);
        url += savePath;
        url += fileName;

        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            resource.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    filePic.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath + fileName)));
        return url;
    }

    public File getAppImageDir(){
        return mAppRootDir;
    }

}
