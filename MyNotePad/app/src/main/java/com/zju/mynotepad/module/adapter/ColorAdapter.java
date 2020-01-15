package com.zju.mynotepad.module.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.zju.mynotepad.R;
import com.zju.mynotepad.module.main.DrawActivity;
import com.zju.mynotepad.module.main.MainActivity;
import com.zju.mynotepad.widget.ColorView;
import com.zju.mynotepad.widget.shape.DrawShape;

import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.RecyclerAdapter;



public class ColorAdapter extends RecyclerAdapter<Integer> {

    private Context mContext;
    private PopupWindow mWindow;

    public ColorAdapter(Context context, Integer[] data, PopupWindow window) {
        super(context, data);
        mContext = context;
        mWindow = window;
    }

    @Override
    public BaseViewHolder<Integer> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new ColorViewHolder(parent);
    }


    class ColorViewHolder extends BaseViewHolder<Integer> {

        private ColorView mColorView;

        public ColorViewHolder(ViewGroup parent) {
            super(parent, R.layout.main_holder_color);
        }

        @Override
        public void onInitializeView() {
            super.onInitializeView();
            mColorView = findViewById(R.id.color_view);
        }

        @Override
        public void onItemViewClick(Integer color) {
            super.onItemViewClick(color);
            DrawShape.mPaintColor = color;
            if (mContext instanceof DrawActivity) {
                ((DrawActivity) mContext).setShowingColorSelector(false);
            }
            if(mWindow.isShowing()){
                mWindow.dismiss();
            }
        }

        @Override
        public void setData(Integer color) {
            super.setData(color);
            if (mContext instanceof MainActivity && DrawShape.mPaintColor == color) {
                mColorView.setColor(color, true);
            }else {
                mColorView.setColor(color, false);
            }
        }
    }
}
