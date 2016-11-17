package com.geri.app.demo.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geri.app.demo.adapter.ContactsAdapter;
import com.geri.app.demo.ui.ChatActivity;
import com.geri.app.demo.ui.GroupListActivity;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by Geri on 2016/11/16.
 */

public class ContactsFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ListView contactsView;
    private List<String> mUserList;
    private ContactsAdapter adapter;
    // 创建新会话对话框
    private AlertDialog addContactsDialog;
    private AlertDialog.Builder alertDialogBuilder;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("ContactsFragment", "mUserList.size():" + mUserList.size());
            adapter = new ContactsAdapter(getActivity(),mUserList);
            contactsView.setAdapter(adapter);

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
        contactsView = (ListView) view.findViewById(R.id.contactsView);

        getFriendList();

        group_ll.setOnClickListener(this);
        tvAdd.setOnClickListener(this);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),ChatActivity.class);
                intent.putExtra("ec_chat_id",mUserList.get(position));
                intent.putExtra("type", EMConversation.EMConversationType.Chat);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //群组列表
            case R.id.group_ll:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            //添加好友
            case R.id.tv_add:
                newSessionDialog();
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

    //申请加好友
    public void addFriend(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(name)) {
                    //获取当前登录用户的username
                    String currUsername = EMClient.getInstance().getCurrentUser();
                    if (name.equals(currUsername)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "不能加自己为好友", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        //参数为要添加的好友的username和添加理由
                        try {
                            EMClient.getInstance().contactManager().addContact(name, "交个朋友吧");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "申请加"+ name + "为好友成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    //弹出新会话的Dialog
    public void newSessionDialog() {
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("添加好友");
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_communal, null);
        final EditText editText = (EditText) view.findViewById(R.id.ml_dialog_edit_input);
        editText.setHint("输入用户不能为空");
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "输入内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    addFriend(name);
                }

            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        addContactsDialog = alertDialogBuilder.create();
        addContactsDialog.show();
    }

    //刷新
    @Override
    public void onStart() {
        super.onStart();
        getFriendList();
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        addContactsDialog.dismiss();
    }
}
