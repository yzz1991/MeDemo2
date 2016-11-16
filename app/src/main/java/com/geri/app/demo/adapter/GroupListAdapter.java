package com.geri.app.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geri.app.ui.R;

import java.util.List;

/**
 * Created by Geri on 2016/11/3.
 */
public class GroupListAdapter extends BaseAdapter{

    private Context mContext;
    private List<String> mMembersList;
    private LayoutInflater mInflater;
    public GroupListAdapter(Context context , List<String> membersList) {
        mContext = context;
        mMembersList = membersList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mMembersList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_group_lv,null);
        }
        TextView tv_group_username = (TextView) convertView.findViewById(R.id.tv_group_username);
        tv_group_username.setText(mMembersList.get(position));
        return convertView;
    }
}
