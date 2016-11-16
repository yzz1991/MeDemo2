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
 * Created by Geri on 2016/11/4.
 */
public class BlockListAdapter extends BaseAdapter{
    private Context mContext;
    private List<String> mBlockedUsers;
    private LayoutInflater mInflater;
    public BlockListAdapter(Context context, List<String> blockedUsers) {
        mContext = context;
        mBlockedUsers = blockedUsers;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mBlockedUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mBlockedUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_blockgroup_lv,null);
        }
        TextView tv_group_username = (TextView) convertView.findViewById(R.id.tv_group_username);
        tv_group_username.setText(mBlockedUsers.get(position));
        return convertView;
    }
}
