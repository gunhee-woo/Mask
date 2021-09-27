package com.dlog.mask.views;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.dlog.mask.R;

public class BoundInfoView extends LinearLayout  {

    public BoundInfoView(Context context){
        super(context);
        initView();
    }
    public BoundInfoView(Context context , @Nullable AttributeSet attr){
        super(context,attr);
        initView();
    }
    public BoundInfoView(Context context , @Nullable AttributeSet attr,int  defstyleAttr){
        super(context,attr,defstyleAttr);
        initView();
    }
    private void initView(){
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater lf = (LayoutInflater)getContext().getSystemService(infService);
        View v = lf.inflate(R.layout.bound_icon_view,this,false);
        addView(v);
    }
}
