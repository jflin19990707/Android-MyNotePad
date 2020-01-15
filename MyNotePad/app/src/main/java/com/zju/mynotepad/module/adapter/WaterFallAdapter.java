package com.zju.mynotepad.module.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zju.mynotepad.bean.Note;
import com.zju.mynotepad.R;
import com.zju.mynotepad.module.main.MainActivity;
import com.zju.mynotepad.util.ImageUtils;


import java.util.ArrayList;
import java.util.List;

public class WaterFallAdapter extends RecyclerView.Adapter<WaterFallAdapter.MyViewHolder>
        implements View.OnClickListener, View.OnLongClickListener{

    private Context mContext;
    private List<Note> mNote; //定义数据源
    private OnRecyclerViewItemClickListener mOnItemClickListener ;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener ;

    //定义构造方法，默认传入上下文和数据源
    public WaterFallAdapter() {
        mNote = new ArrayList<>();
    }
    public void setmNotes(List<Note> notes) {
        this.mNote = notes;
    }

    @Override  //将ItemView渲染进来，创建ViewHolder
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_note, null);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new MyViewHolder(view);
    }


    @Override  //将数据源的数据绑定到相应控件上
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Note note = mNote.get(position);
        holder.itemView.setTag(note);
        Uri uri = ImageUtils.getUriFromPath(note.getUrl());

        holder.graph.setImageURI(uri);
        holder.graph.getLayoutParams().height = (position % 2)*100 + 400; //从数据源中获取图片高度，动态设置到控件上
        holder.graph.getLayoutParams().width = (MainActivity.widthPixels-20)/2;
        holder.note_title.setText(note.getTitle());
        holder.note_time.setText(note.getCreateTime());
    }

    @Override
    public int getItemCount() {
        if (mNote != null && mNote.size()>0) {
            return mNote.size();
        }
        return 0;
    }

    //定义自己的ViewHolder，将View的控件引用在成员变量上
    public class MyViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView graph;
        TextView note_title;
        TextView note_time;

        public MyViewHolder(View itemView) {
            super(itemView);
            graph = (SimpleDraweeView) itemView.findViewById(R.id.graph_content);
            note_title = (TextView) itemView.findViewById(R.id.item_note_title);
            note_time = (TextView) itemView.findViewById(R.id.item_note_time);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Note)v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v,(Note)v.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Note note);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view , Note note);
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }
}