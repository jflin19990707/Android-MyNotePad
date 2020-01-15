package com.zju.mynotepad.bean;

import com.zju.mynotepad.widget.shape.ShapeResource;

import java.io.Serializable;
import java.util.List;


public class Notepad implements Serializable {

    public String mFileName;
    public String mTitle;
    public long mCreateTime;
    public List<ShapeResource> mPaths;
    public String url;
}
