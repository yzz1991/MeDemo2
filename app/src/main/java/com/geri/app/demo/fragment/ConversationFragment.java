package com.geri.app.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.geri.app.demo.adapter.ConversationAdapter;
import com.geri.app.demo.ui.ApplicationNoticeActivity;
import com.geri.app.demo.ui.ChatActivity;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geri on 2016/11/16.
 */

public class ConversationFragment extends Fragment{

    private View view;
    private List<EMConversation> list;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation,null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
    }

    private void initView() {
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setText("会话");
        ListView conversationView = (ListView) view.findViewById(R.id.conversationView);
        //加载会话集合
        loadConversationList();

        //设置适配器
        adapter = new ConversationAdapter(list,getActivity());
        conversationView.setAdapter(adapter);
        conversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.get(position).conversationId().equals("通知")){
                    startActivity(new Intent(getActivity(),ApplicationNoticeActivity.class)
                            .putExtra("groupId", list.get(position).conversationId()));
                }else{
                    // 跳转到聊天界面，开始聊天
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("ec_chat_id", list.get(position).conversationId());
                    intent.putExtra("type", list.get(position).getType());
                    startActivity(intent);
                }

            }
        });

    }

    //所有会话
    private void loadConversationList() {
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        list = new ArrayList<>();
        synchronized (conversations){
            for(EMConversation temp : conversations.values()){
                list.add(temp);
            }

        }
    }

    //刷新
    @Override
    public void onStart() {
        super.onStart();
        loadConversationList();
        adapter.notifyDataSetChanged();
    }
}
