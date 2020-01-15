package com.zju.mynotepad.module.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zju.mynotepad.bean.Note;
import com.zju.mynotepad.db.NoteDao;
import com.zju.mynotepad.R;
import com.zju.mynotepad.module.adapter.WaterFallAdapter;

import java.util.List;

public class FragmentNote extends androidx.fragment.app.Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private WaterFallAdapter wfAdapter;
    private FloatingActionButton mfab;
    private List<Note> noteList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =View.inflate(getContext(), R.layout.fragment_note,null);
        initView(view);
        return view;
    }
    public void initView(View view){


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        wfAdapter = new WaterFallAdapter();
        wfAdapter.setmNotes(noteList);


        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(wfAdapter);

        wfAdapter.setOnItemClickListener(new WaterFallAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });

        wfAdapter.setOnItemLongClickListener(new WaterFallAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = MainActivity.noteDao.deleteNote(note.getId());
                        if (ret > 0){
                            //TODO 删除笔记成功后，记得删除图片（分为本地图片和网络图片）
//                            StringUtils.getTextFromHtml(note.getContent(), true);
                            refreshNoteList();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });

        mfab = (FloatingActionButton) view.findViewById(R.id.fab);
        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddNoteActivity.class);
                intent.putExtra("groupName", "默认笔记");
                intent.putExtra("flag", 0);
                startActivity(intent);
            }
        });
    }

    //刷新笔记列表
    private void refreshNoteList(){
        if (MainActivity.noteDao == null)
            MainActivity.noteDao = new NoteDao(getContext());
        noteList = MainActivity.noteDao.queryNotesAll(0);
        wfAdapter.setmNotes(noteList);
        wfAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshNoteList();
    }
}
