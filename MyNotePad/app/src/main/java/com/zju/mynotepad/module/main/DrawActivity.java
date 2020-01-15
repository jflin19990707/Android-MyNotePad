package com.zju.mynotepad.module.main;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.zju.mynotepad.R;
import com.zju.mynotepad.app.Config;
import com.zju.mynotepad.data.AccountModel;
import com.zju.mynotepad.bean.Notepad;
import com.zju.mynotepad.module.adapter.ColorAdapter;
import com.zju.mynotepad.widget.BoardView;
import com.zju.mynotepad.widget.FloatAdapter;
import com.zju.mynotepad.widget.FloatViewGroup;
import com.zju.mynotepad.widget.InputDialog;
import com.zju.mynotepad.widget.shape.DrawShape;
import com.zju.mynotepad.widget.shape.ShapeResource;

import java.util.Collections;
import java.util.List;

import cn.lemon.common.base.ToolbarActivity;
import cn.lemon.common.base.presenter.RequirePresenter;
import cn.lemon.view.RefreshRecyclerView;

@RequirePresenter(MainPresenter.class)
public class DrawActivity extends ToolbarActivity<MainPresenter> {
        private BoardView mBoardView;
        private FloatViewGroup mFloatViews;
        private FloatAdapter mAdapter;
        private Handler mHandler;

        private boolean isShowingColorSelector = false;
        private boolean isShowingSizeSelector = false;

        private PopupWindow mColorWindow;
        private PopupWindow mSizeWindow;
        private String url = "";

        //    private DrawerLayout mDrawer;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                setToolbarHomeBack(false);
                setContentView(R.layout.drawboard_activity);

                mHandler = new Handler(getMainLooper());

                mFloatViews = (FloatViewGroup) findViewById(R.id.float_view_group);
                mBoardView = (BoardView) findViewById(R.id.board_view);

                mAdapter = new FunctionAdapter(this, mBoardView);
                mFloatViews.setAdapter(mAdapter);
                mBoardView.setOnDownAction(new BoardView.OnDownAction() {
                        @Override
                        public void dealDownAction() {
                                mFloatViews.checkShrinkViews();
                        }
                });
                mBoardView.setintent(getIntent(),getPresenter());
        }

        public void getData(){
                AccountModel.getInstance().getNoteList(new AccountModel.NoteCallback() {
                        @Override
                        public void onCallback(List<Notepad> data) {
                                Collections.reverse(data);
                                FragmentDraw.setData(data);
                        }
                });
        }

        @Override
        public void onBackPressed() {
                super.onBackPressed();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.main, menu);
                return true;
        }

        //选择点击按钮
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.recall:
                                mBoardView.reCall();
                                break;
                        case R.id.recover:
                                mBoardView.undo();
                                break;
                        case R.id.save_note:
                                Bitmap btmp = mBoardView.getDrawBitmap();
                                url = getPresenter().saveImage(btmp,this);
                                showNoteDialog();
                                break;
                        case R.id.save_image_to_app:
                                Bitmap bmp = mBoardView.getDrawBitmap();
                                getPresenter().saveImage(bmp,this);
                                break;
                        case R.id.color:
                                if (isShowingColorSelector) {
                                        mColorWindow.dismiss();
                                        isShowingColorSelector = false;
                                } else {
                                        showColorSelectorWindow();
                                }
                                break;
                        case R.id.size:
                                if (isShowingSizeSelector) {
                                        mSizeWindow.dismiss();
                                        isShowingSizeSelector = false;
                                } else {
                                        showSizeSelectorWindow();
                                }
                                break;
                        default:break;
                }
         return true;
        }


        //保存笔迹
        public void showNoteDialog() {

        final InputDialog noteDialog = new InputDialog(this);
                noteDialog.setCancelable(false);
                noteDialog.setTitle("请输入标题");
                noteDialog.setHint("标题");
                if (getPresenter().getLocalNote() != null) {
                noteDialog.setContent(getPresenter().getLocalNote().mTitle);
                }
                noteDialog.show();
                noteDialog.setPositiveClickListener(new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
                getPresenter().saveNote(noteDialog.getContent(),mBoardView.getNotePath(),url);
                noteDialog.dismiss();
                mBoardView.clearScreen();
                }
                });
                noteDialog.setPassiveClickListener(new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
                noteDialog.dismiss();
                mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
                mBoardView.updateBitmap();
                }
                }, 100);
                }
                });
        }

        public void updateDrawPaths(List<ShapeResource> paths){
                mBoardView.updateDrawFromPaths(paths);
        }

        //设置画笔大小
        public void showSizeSelectorWindow() {
                if (isShowingSizeSelector) {
                        return;
                } else if (isShowingColorSelector) {
                        mColorWindow.dismiss();
                        isShowingColorSelector = false;
                }
                isShowingSizeSelector = true;
                View view = LayoutInflater.from(this).inflate(R.layout.main_window_size_selector, null);
                SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
                 final TextView size = (TextView) view.findViewById(R.id.size);
                int numSize = (int) DrawShape.mPaintWidth;
                seekBar.setProgress(numSize);
                size.setText(numSize + "");

                mSizeWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
                mSizeWindow.showAsDropDown(getToolbar());
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        size.setText(progress + "");
                        DrawShape.mPaintWidth = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                        mSizeWindow.dismiss();
                        isShowingSizeSelector = false;
                        }
                });
        }

        //颜色选择器
        public void showColorSelectorWindow(){
                if (isShowingColorSelector) {
                return;
                } else if (isShowingSizeSelector) {
                mSizeWindow.dismiss();
                isShowingSizeSelector = false;
                }
                isShowingColorSelector = true;
                View view = LayoutInflater.from(this).inflate(R.layout.main_window_color_selector, null);
                mColorWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

                RefreshRecyclerView recyclerView = (RefreshRecyclerView) view.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new GridLayoutManager(this,4));
                ColorAdapter adapter = new ColorAdapter(this, Config.COLORS, mColorWindow);
                recyclerView.setAdapter(adapter);

                mColorWindow.showAsDropDown(getToolbar());
        }

        public void setShowingColorSelector(boolean b) {
                isShowingColorSelector = b;
        }
}
