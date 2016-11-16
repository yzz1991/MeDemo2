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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geri on 2016/10/26.
 */
public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView conversation_back;
    private List<EMConversation> list;
    private ConversationAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        initView();

    }

    private void initView() {
        conversation_back = (TextView) findViewById(R.id.conversation_back);
        ListView lv = (ListView) findViewById(R.id.conversation_lv);
        conversation_back.setOnClickListener(this);
        //加载会话集合
        loadConversationList();

        //设置适配器
        adapter = new ConversationAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.get(position).conversationId().equals("通知")){
                    startActivity(new Intent(ConversationActivity.this,ApplicationNoticeActivity.class)
                            .putExtra("groupId", list.get(position).conversationId()));
                }else{
                    // 跳转到聊天界面，开始聊天
                    Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
                    intent.putExtra("ec_chat_id", list.get(position).conversationId());
                    intent.putExtra("type", list.get(position).getType());
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.conversation_back:
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadConversationList();
        adapter.notifyDataSetChanged();
    }

    private void loadConversationList() {
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        list = new ArrayList<>();
        synchronized (conversations){
            for(EMConversation temp : conversations.values()){
                list.add(temp);
            }

        }
    }

    class ConversationAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(ConversationActivity.this);
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_conversation,null);
            }
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            EMGroup group = EMClient.getInstance().groupManager().getGroup(list.get(position).conversationId());
            if(list.get(position).isGroup() && group != null){
                tv_username.setText(group.getGroupName());
            }else{
                tv_username.setText(list.get(position).getUserName());
            }
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
