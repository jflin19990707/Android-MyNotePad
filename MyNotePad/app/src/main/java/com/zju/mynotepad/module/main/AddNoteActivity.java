package com.zju.mynotepad.module.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ielse.imagewatcher.ImageWatcherHelper;
import com.sendtion.xrichtext.RichTextEditor;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zju.mynotepad.R;
import com.zju.mynotepad.bean.Group;
import com.zju.mynotepad.bean.Note;
import com.zju.mynotepad.comm.GlideSimpleLoader;
import com.zju.mynotepad.comm.MyGlideEngine;
import com.zju.mynotepad.db.GroupDao;
import com.zju.mynotepad.db.NoteDao;
import com.zju.mynotepad.util.CommonUtil;
import com.zju.mynotepad.util.ImageUtils;
import com.zju.mynotepad.util.SDCardUtil;
import com.zju.mynotepad.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import io.reactivex.schedulers.Schedulers;


public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CODE = 0;
    private ImageView iv_picture;
    private ImageView iv_checkBox;
    private ImageView iv_skin;
    private ImageView iv_textStyle;
    private ImageView iv_sound;
    private ImageView iv_ok;
    private ImageView iv_cancel;

    private ImageView blue_skin;
    private ImageView red_skin;
    private ImageView yellow_skin;
    private ImageView green_skin;

    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;


    private Uri mImageUri, mImageUriFromFile;
    private File imageFile;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_PERMISSION_CODE = 267;
    private static final int TAKE_PHOTO = 189;
    private static final int CHOOSE_PHOTO = 385;
    private static final String FILE_PROVIDER_AUTHORITY = "com.zju.mynotepad.fileprovider";
    private final String TAG = getClass().getSimpleName();
    private Bitmap orc_bitmap;//拍照和相册获取图片的Bitmap

    private EditText note_title;
    private RichTextEditor richEditText;
    private TextView note_time;
    private TextView note_group;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private Note note;//笔记对象

    private String myTitle;
    private String myContent;
    private String myGroupName;
    private String myNoteTime;
    private int flag;//区分是新建笔记还是编辑笔记

    private static final int cutTitleLength = 20;//截取的标题长度

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Disposable subsLoading;
    private Disposable subsInsert;
    private ImageWatcherHelper iwHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        initView();

        /*申请读取存储的权限*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERMISSION_WRITE_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1 );
            }
        }
    }
    public void initView(){
        iv_picture = (ImageView)findViewById(R.id.iv_picture);
        iv_checkBox = (ImageView)findViewById(R.id.iv_checkBox);
        iv_skin = (ImageView)findViewById(R.id.iv_skin);
        iv_textStyle = (ImageView)findViewById(R.id.iv_textStyle);
        iv_sound = (ImageView)findViewById(R.id.iv_sound);
        richEditText = (RichTextEditor) findViewById(R.id.note_content);

        iv_ok = (ImageView)findViewById(R.id.ok_add);
        iv_cancel = (ImageView)findViewById(R.id.cancel_add);

        iv_picture.setOnClickListener(this);
        iv_checkBox.setOnClickListener(this);
        iv_skin.setOnClickListener(this);
        iv_textStyle.setOnClickListener(this);
        iv_sound.setOnClickListener(this);

        iv_ok.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);

        iwHelper = ImageWatcherHelper.with(this, new GlideSimpleLoader());
        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);
        note = new Note();

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        note_title = findViewById(R.id.note_title);
        note_time = findViewById(R.id.note_time);
        note_group = findViewById(R.id.note_group);

        try {
            Intent intent = getIntent();
            flag = intent.getIntExtra("flag", 0);//0新建，1编辑
            if (flag == 1){//编辑
                Bundle bundle = intent.getBundleExtra("data");
                note = (Note) bundle.getSerializable("note");

                if (note != null) {
                    myTitle = note.getTitle();
                    myContent = note.getContent();
                    myNoteTime = note.getCreateTime();
                    Group group = groupDao.queryGroupById(note.getGroupId());
                    if (group != null){
                        myGroupName = group.getName();
                        note_group.setText(myGroupName);
                    }

                    loadingDialog = new ProgressDialog(this);
                    loadingDialog.setMessage("数据加载中...");
                    loadingDialog.setCanceledOnTouchOutside(false);
                    loadingDialog.show();

                    note_time.setText(note.getCreateTime());
                    note_title.setText(note.getTitle());
                    richEditText.post(new Runnable() {
                        @Override
                        public void run() {
                            dealWithContent();
                        }
                    });
                }
            } else {
                if (myGroupName == null || "全部笔记".equals(myGroupName)) {
                    myGroupName = "默认笔记";
                }
                note_group.setText(myGroupName);
                myNoteTime = CommonUtil.date2string(new Date());
                note_time.setText(myNoteTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.cancel_add:
                finish();
                break;
            case R.id.ok_add:
                try {
                    saveNoteData(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.iv_picture: //获取相册图片
                openAlbum();
                break;
            case R.id.iv_checkBox:
                break;
            case R.id.iv_skin:
                showSkinDialog(view);
                break;
            case R.id.iv_textStyle:
                break;
            case R.id.iv_sound:
                break;
            case R.id.blue_skin:
                richEditText.setBackgroundColor(getColor(R.color.md_light_blue_200_color_code));
                dialog.dismiss();
                break;
            case R.id.red_skin:
                richEditText.setBackgroundColor(getColor(R.color.md_red_200_color_code));
                dialog.dismiss();
                break;
            case R.id.yellow_skin:
                richEditText.setBackgroundColor(getColor(R.color.md_yellow_200_color_code));
                dialog.dismiss();
                break;
            case R.id.green_skin:
                richEditText.setBackgroundColor(getColor(R.color.md_green_200_color_code));
                dialog.dismiss();
                default:
                    break;

        }
    }

    private void showSkinDialog(View view) {
        dialog = new Dialog(this,R.style.ActionSheetDialogStyle);
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_skin,null);

        blue_skin = (ImageView)inflate.findViewById(R.id.blue_skin);
        red_skin = (ImageView)inflate.findViewById(R.id.red_skin);
        yellow_skin = (ImageView)inflate.findViewById(R.id.yellow_skin);
        green_skin = (ImageView)inflate.findViewById(R.id.green_skin);

        blue_skin.setOnClickListener(this);
        red_skin.setOnClickListener(this);
        yellow_skin.setOnClickListener(this);
        green_skin.setOnClickListener(this);

        dialog.setContentView(inflate);

        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
        //将属性设置给窗体
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                insertImagesSync(data);
                break;
            default:
                break;
        }
    }

    /**
     * 将Uri转化为路径
     * @param uri 要转化的Uri
     * @param selection 4.4之后需要解析Uri，因此需要该参数
     * @return 转化之后的路径
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 打开相册
     */
    private void openAlbum() {

        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(3)//最大选择数量为9
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true,FILE_PROVIDER_AUTHORITY))//存储到哪里
                .forResult(CHOOSE_PHOTO);//请求码
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orc_bitmap != null) {
            orc_bitmap.recycle();
        } else {
            orc_bitmap = null;
        }
    }


    private String getEditData() {
        StringBuilder content = new StringBuilder();
        try {
            List<RichTextEditor.EditData> editList = richEditText.buildEditData();
            for (RichTextEditor.EditData itemData : editList) {
                if (itemData.inputStr != null) {
                    content.append(itemData.inputStr);
                } else if (itemData.imagePath != null) {
                    content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }


    /**
     * 显示数据
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        }catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    private void dealWithContent(){
        //showEditData(note.getContent());
        richEditText.clearAllLayout();
        showDataSync(note.getContent());

        // 图片删除事件
        richEditText.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        showToast("删除成功：" + imagePath);
                    }
                }
            }
        });
        // 图片点击事件
        richEditText.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, String imagePath) {
                try {
                    myContent = getEditData();
                    if (!TextUtils.isEmpty(myContent)){
                        List<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                        if (!TextUtils.isEmpty(imagePath)) {
                            int currentPosition = imageList.indexOf(imagePath);
                            showToast("点击图片：" + currentPosition + "：" + imagePath);

                            List<Uri> dataList = new ArrayList<>();
                            for (int i = 0; i < imageList.size(); i++) {
                                dataList.add(ImageUtils.getUriFromPath(imageList.get(i)));
                            }
                            iwHelper.show(dataList, currentPosition);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 异步方式插入图片
     */
    private void insertImagesSync(final Intent data){
        insertDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try{
                    richEditText.measure(0, 0);
                    List<Uri> mSelected = Matisse.obtainResult(data);
                    // 可以同时插入多张图片
                    for (Uri imageUri : mSelected) {
                        String imagePath = SDCardUtil.getFilePathFromUri(AddNoteActivity.this,  imageUri);
                        //Log.e(TAG, "###path=" + imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //Log.e(TAG, "###imagePath="+imagePath);
                        emitter.onNext(imagePath);
                    }

                    emitter.onComplete();
                }catch (Exception e){
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        })
                //.onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        showToast("图片插入失败:"+e.getMessage());
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsInsert = d;
                    }

                    @Override
                    public void onNext(String imagePath) {
                        richEditText.insertImage(imagePath);
                    }
                });
    }

    /**
     * 异步方式显示数据
     */
    private void showDataSync(final String html){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
                //.onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        if (richEditText != null) {
                            //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                            richEditText.addEditTextAtIndex(richEditText.getLastIndex(), "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsLoading = d;
                    }

                    @Override
                    public void onNext(String text) {
                        try {
                            if (richEditText != null) {
                                if (text.contains("<img") && text.contains("src=")) {
                                    //imagePath可能是本地路径，也可能是网络地址
                                    String imagePath = StringUtils.getImgSrc(text);
                                    //Log.e("---", "###imagePath=" + imagePath);
                                    //插入空的EditText，以便在图片前后插入文字
                                    richEditText.addEditTextAtIndex(richEditText.getLastIndex(), "");
                                    richEditText.addImageViewAtIndex(richEditText.getLastIndex(), imagePath);
                                } else {
                                    richEditText.addEditTextAtIndex(richEditText.getLastIndex(), text);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /** 显示吐司 **/
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(boolean isBackground) {
        String noteTitle = note_title.getText().toString();
        String noteContent = getEditData();
        String groupName = note_group.getText().toString();
        String noteTime = note_time.getText().toString();
        String url = "";
        url = StringUtils.getTextFromHtml(noteContent,true).get(0);



        try {
            Group group = groupDao.queryGroupByName(myGroupName);
            if (group != null) {
                if (noteTitle.length() == 0 ){//如果标题为空，则截取内容为标题
                    if (noteContent.length() > cutTitleLength){
                        noteTitle = noteContent.substring(0,cutTitleLength);
                    } else if (noteContent.length() > 0){
                        noteTitle = noteContent;
                    }
                }
                int groupId = group.getId();
                note.setTitle(noteTitle);
                note.setContent(noteContent);
                note.setGroupId(groupId);
                note.setGroupName(groupName);
                note.setType(2);
                note.setBgColor("#FFFFFF");
                note.setIsEncrypt(0);
                note.setCreateTime(CommonUtil.date2string(new Date()));
                if(!url.equals("")){
                    note.setUrl(url);
                }else{
                    note.setUrl(null);
                }
                if (flag == 0 ) {//新建笔记
                    if (noteTitle.length() == 0 && noteContent.length() == 0) {
                        if (!isBackground){
                            Toast.makeText(AddNoteActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        long noteId = noteDao.insertNote(note);
                        //Log.i("", "noteId: "+noteId);
                        //查询新建笔记id，防止重复插入
                        note.setId((int) noteId);
                        flag = 1;//插入以后只能是编辑
                        if (!isBackground){
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }else if (flag == 1) {//编辑笔记
                    if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                            || !groupName.equals(myGroupName) || !noteTime.equals(myNoteTime)) {
                        noteDao.updateNote(note);
                    }
                    if (!isBackground){
                        finish();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //如果APP处于后台，或者手机锁屏，则保存数据
            if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                    CommonUtil.isLockScreeen(getApplicationContext())){
                saveNoteData(true);//处于后台时保存数据
            }

            if (subsLoading != null && subsLoading.isDisposed()){
                subsLoading.dispose();
            }
            if (subsInsert != null && subsInsert.isDisposed()){
                subsInsert.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit(){
        try {
            String noteTitle = note_title.getText().toString();
            String noteContent = getEditData();
            String groupName = note_group.getText().toString();
            String noteTime = note_time.getText().toString();
            if (flag == 0) {//新建笔记
                if (noteTitle.length() > 0 || noteContent.length() > 0) {
                    saveNoteData(false);
                }
            }else if (flag == 1) {//编辑笔记
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                        || !groupName.equals(myGroupName) || !noteTime.equals(myNoteTime)) {
                    saveNoteData(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!iwHelper.handleBackPressed()) {
            super.onBackPressed();
        }
        dealwithExit();
    }
}
