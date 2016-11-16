package com.geri.app.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geri.app.demo.adapter.ContactsAdapter;
import com.geri.app.demo.ui.GroupListActivity;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by Geri on 2016/11/16.
 */

public class ContactsFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ListView mConversationView;
    private List<String> mUserList;
    private ContactsAdapter adapter;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("ContactsFragment", "mUserList.size():" + mUserList.size());
            adapter = new ContactsAdapter(getActivity(),mUserList);
            mConversationView.setAdapter(adapter);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts,null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setText("联系人");
        TextView tvAdd = (TextView) view.findViewById(R.id.tv_add);
        LinearLayout group_ll = (LinearLayout) view.findViewById(R.id.group_ll);
        mConversationView = (ListView) view.findViewById(R.id.contactsView);

        getFriendList();

        group_ll.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.group_ll:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
        }
    }

    //获取好友列表
    public void getFriendList() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mUserList = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    handler.sendEmptyMessage(0);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    int errorCode = e.getErrorCode();
                    switch (errorCode) {

                    }
                }
            }
        }).start();


    }
}
