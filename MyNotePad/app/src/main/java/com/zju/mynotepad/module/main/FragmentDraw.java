package com.zju.mynotepad.module.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zju.mynotepad.R;
import com.zju.mynotepad.data.AccountModel;
import com.zju.mynotepad.bean.Notepad;
import com.zju.mynotepad.module.adapter.NotepadAdapter;

import java.util.Collections;
import java.util.List;

import cn.lemon.view.RefreshRecyclerView;
import cn.lemon.view.adapter.Action;

public class FragmentDraw extends androidx.fragment.app.Fragment {

    private static RefreshRecyclerView mRecyclerView;
    private static NotepadAdapter mAdapter;
    private FloatingActionButton mDrawfab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =View.inflate(getContext(), R.layout.fragment_draw,null);
        mRecyclerView = (RefreshRecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = MainActivity.getmAdapter();
        mRecyclerView.setAdapter(mAdapter);

        AccountModel.getInstance().getNoteList(new AccountModel.NoteCallback() {
            @Override
            public void onCallback(List<Notepad> data) {
                Collections.reverse(data);
                setData(data);
            }
        });

        mRecyclerView.addRefreshAction(new Action() {
            @Override
            public void onAction() {
                AccountModel.getInstance().getNoteList(new AccountModel.NoteCallback() {
                    @Override
                    public void onCallback(List<Notepad> data) {
                        Collections.reverse(data);
                        setData(data);
                    }
                });
            }
        });

        mDrawfab = (FloatingActionButton) view.findViewById(R.id.draw);
        mDrawfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DrawActivity.class);
                startActivityForResult(intent, 1111);
            }
        });
        return view;
    }

    public static void setData(List<Notepad> data){
        mAdapter.clear();
        mAdapter.addAll(data);
        mRecyclerView.dismissSwipeRefresh();
    }

}
