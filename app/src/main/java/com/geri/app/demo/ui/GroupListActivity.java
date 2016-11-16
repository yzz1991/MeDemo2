package com.geri.app.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;

import java.util.List;

/**
 * Created by Geri on 2016/11/4.
 */
public class GroupListActivity extends AppCompatActivity{

    private List<EMGroup> grouplist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        initView();
    }

    private void initView() {
        TextView conversation_back = (TextView) findViewById(R.id.conversation_back);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("群组列表");
        ListView lv = (ListView) findViewById(R.id.conversation_lv);
        getGroupList();
        //设置适配器
        lv.setAdapter(new GroupUserListAdapter());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到聊天界面，开始聊天
                Intent intent = new Intent(GroupListActivity.this, ChatActivity.class);
                intent.putExtra("ec_chat_id", grouplist.get(position).getGroupId());
                intent.putExtra("type", EMConversation.EMConversationType.GroupChat);
                startActivity(intent);
                finish();
            }
        });
        conversation_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupListActivity.this,MainActivity.class));
                finish();
            }
        });
    }

    //获取群组列表
    public void getGroupList() {
        //从本地加载群组列表
        grouplist = EMClient.getInstance().groupManager().getAllGroups();
    }

    class GroupUserListAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return grouplist.size();
        }

        @Override
        public Object getItem(int position) {
            return grouplist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(GroupListActivity.this);
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_conversation,null);
            }
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            tv_username.setText(grouplist.get(position).getGroupName());
            return convertView;
        }
    }
}
