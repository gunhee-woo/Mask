package com.dlog.mask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RcylNoticeAdapter extends RecyclerView.Adapter<RcylNoticeAdapter.ViewHolder> {
    ArrayList<Notice> mNoticeList ;
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txt_noticeInfo;
        ViewHolder(View itemView){
            super(itemView);
            txt_noticeInfo = itemView.findViewById(R.id.txt_rcyl_notice_item);
        }
    }
    RcylNoticeAdapter(ArrayList<Notice> list){
        this.mNoticeList = list;
    }

    @NonNull
    @Override
    public RcylNoticeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rcyl_notice_item_view,parent,false);
        RcylNoticeAdapter.ViewHolder vh = new RcylNoticeAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String noticeInfo = mNoticeList.get(position).mNoticeInfo;
        holder.txt_noticeInfo.setText(noticeInfo);
    }

    @Override
    public int getItemCount() {
        return mNoticeList.size();
    }
}
