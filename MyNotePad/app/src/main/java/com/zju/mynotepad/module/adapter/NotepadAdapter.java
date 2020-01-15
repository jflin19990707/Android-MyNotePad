package com.zju.mynotepad.module.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zju.mynotepad.R;
import com.zju.mynotepad.app.Config;
import com.zju.mynotepad.data.AccountModel;
import com.zju.mynotepad.bean.Notepad;
import com.zju.mynotepad.module.main.DrawActivity;
import com.zju.mynotepad.module.main.MainActivity;
import com.zju.mynotepad.util.ImageUtils;

import cn.alien95.util.TimeTransform;
import cn.lemon.common.base.widget.MaterialDialog;
import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.RecyclerAdapter;


public class NotepadAdapter extends RecyclerAdapter<Notepad> {

    private Context mContext;

    public NotepadAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public BaseViewHolder<Notepad> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new NoteViewHolder(parent);
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    private class NoteViewHolder extends BaseViewHolder<Notepad> {

        private TextView mTitle;
        private TextView mTime;
        private SimpleDraweeView graph;

        public NoteViewHolder(ViewGroup parent) {
            super(parent, R.layout.account_holder_notepad);
        }

        @Override
        public void onInitializeView() {
            super.onInitializeView();
            mTitle = findViewById(R.id.item_draw_title);
            mTime = findViewById(R.id.item_draw_time);
            graph = findViewById(R.id.draw_content);
        }

        @Override
        public void onItemViewClick(Notepad object) {
            super.onItemViewClick(object);
            if(mContext instanceof MainActivity){
                Intent intent = new Intent(mContext, DrawActivity.class);
                intent.putExtra(Config.NOTE_DATA,object);
                intent.putExtra("isload",true);
                mContext.startActivity(intent);
            }
        }

        @Override
        public void setData(final Notepad notepad) {
            super.setData(notepad);
            if(notepad != null){
                mTitle.setText(notepad.mTitle);
                mTime.setText(TimeTransform.getRecentlyDate(notepad.mCreateTime));
                Uri uri = ImageUtils.getUriFromPath(notepad.url);
                graph.setImageURI(uri);
                graph.getLayoutParams().height = 600; //从数据源中获取图片高度，动态设置到控件上
                graph.getLayoutParams().width = MainActivity.widthPixels-20;
            }
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteDialog(notepad);
                    return true;
                }
            });
        }

        private void deleteDialog(final Notepad notepad) {
            new MaterialDialog.Builder(itemView.getContext()).setTitle("是否删除")
                    .setCancelable(true)
                    .setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AccountModel.getInstance().deleteNoteFile(notepad.mFileName);
                            remove(notepad);
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
}
