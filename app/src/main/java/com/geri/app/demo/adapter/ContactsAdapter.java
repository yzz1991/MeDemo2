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
 * Created by Geri on 2016/11/16.
 */
public class ContactsAdapter extends BaseAdapter{
    private List<String> mUserList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ContactsAdapter(Context context, List<String> userList) {
        this.mContext = context;
        this.mUserList = userList;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_contacts,null);
            holder = new ViewHolder();
            holder.tvNickName = (TextView) convertView.findViewById(R.id.tv_nickName);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvNickName.setText(mUserList.get(position));
        return convertView;
    }

    class ViewHolder{
        TextView tvNickName;
    }
}
