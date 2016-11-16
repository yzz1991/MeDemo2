package com.geri.app.demo.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.geri.app.demo.adapter.GroupListAdapter;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by Geri on 2016/11/3.
 */
public class GroupDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String groupId;
    private GridView groupGv;
    private GroupListAdapter adapter;
    private TextView addGroup;
    private List<String> membersList;
    private TextView tvGroupName;
    private String owner;
    // 创建新会话对话框
    private AlertDialog createConversationDialog;
    private AlertDialog.Builder alertDialogBuilder;
    private String groupName;
    private Button bt_destroyGroup;
    private TextView clearAll;
    private TextView changeGroupName;
    private TextView blockNameList;
    private Switch blockGroupMessage;
    private LinearLayout blockGroupMessage_ll;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
            membersList.clear();
            membersList.addAll(group.getMembers());
            Log.i("GroupDetailsActivity", "membersList.size():" + membersList.size());
            adapter.notifyDataSetChanged();
        }
    };
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupId = getIntent().getStringExtra("groupId");
        initView();

    }

    private void initView() {
        TextView groupBack = (TextView) findViewById(R.id.group_back);
        tvGroupName = (TextView) findViewById(R.id.tv_groupName);
        addGroup = (TextView) findViewById(R.id.addGroup);
        groupGv = (GridView) findViewById(R.id.groupGv);
        clearAll = (TextView) findViewById(R.id.clearAll);
        changeGroupName = (TextView) findViewById(R.id.changeGroupName);
        blockNameList = (TextView) findViewById(R.id.blockNameList);
        blockGroupMessage = (Switch) findViewById(R.id.blockGroupMessage);
        blockGroupMessage_ll = (LinearLayout) findViewById(R.id.blockGroupMessage_ll);
        bt_destroyGroup = (Button) findViewById(R.id.bt_destroyGroup);
        sp = getSharedPreferences("status", Context.MODE_PRIVATE);
        edit = sp.edit();
        if(sp.getBoolean("isChecked",false)){
            blockGroupMessage.setChecked(true);
        }else{
            blockGroupMessage.setChecked(false);
        }
        getGroupInfo();
        groupBack.setOnClickListener(this);
        addGroup.setOnClickListener(this);
        bt_destroyGroup.setOnClickListener(this);
        clearAll.setOnClickListener(this);
        changeGroupName.setOnClickListener(this);
        blockNameList.setOnClickListener(this);
        blockGroupMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!EMClient.getInstance().getCurrentUser().equals(owner)){
                    if (isChecked) {
                        blockGroupMessage();
                        edit.putBoolean("isChecked",true);
                    } else {
                        unBlockGroupMessage();
                        edit.putBoolean("isChecked",false);
                    }
                    edit.commit();
                } else{
                    Toast.makeText(GroupDetailsActivity.this, "是群主，不能屏蔽群消息", Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.registerForContextMenu(groupGv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //返回上级页面
            case R.id.group_back:
                finish();
                break;
            //邀请人进群
            case R.id.addGroup:
                initDialog();
                break;
            //解散群组或退出群组
            case R.id.bt_destroyGroup:
                if(EMClient.getInstance().getCurrentUser().equals(owner)){
                    destroyGroup();
                }else{
                    leaveGroup();
                }
                startActivity(new Intent(this,MainActivity.class));
                finish();
                break;
            //清空聊天记录
            case R.id.clearAll:
                deleteConversation();
                break;
            //修改群名称
            case R.id.changeGroupName:
                if(EMClient.getInstance().getCurrentUser().equals(owner)){
                    changeGroupNameDialog();
                }
                break;
            //获取黑名单
            case R.id.blockNameList:
                if(EMClient.getInstance().getCurrentUser().equals(owner)){
                    startActivity(new Intent(GroupDetailsActivity.this, BlockListActivity.class).putExtra("groupId",groupId));
                }
                break;

        }
    }

    //弹出加人的Dialog
    public void initDialog() {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("邀请进群");
        View view = this.getLayoutInflater().inflate(R.layout.dialog_communal, null);
        TextView textView = (TextView) view.findViewById(R.id.ml_dialog_text_message);
        textView.setText("请输入对方的username");
        final EditText editText = (EditText) view.findViewById(R.id.ml_dialog_edit_input);
        editText.setHint("输入内容不能为空");
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(GroupDetailsActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    addgroup(name);
                    Toast.makeText(GroupDetailsActivity.this, name+"加群成功", Toast.LENGTH_SHORT).show();

                }

            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        createConversationDialog = alertDialogBuilder.create();
        createConversationDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroupInfo();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, Menu.NONE, "踢出群");
        menu.add(0, 1, Menu.NONE, "加入黑名单");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case 0:
                if(EMClient.getInstance().getCurrentUser().equals(owner)){
                    removeUserGroup(membersList.get(menuInfo.position));
                    Toast.makeText(this, "踢出操作OK", Toast.LENGTH_SHORT).show();
                    getGroupInfo();
                }else{
                    Toast.makeText(this, "不是群主,不能执行踢出操作", Toast.LENGTH_SHORT).show();
                }

                break;
            case 1:
                if(EMClient.getInstance().getCurrentUser().equals(owner)){
                    addblackUser(membersList.get(menuInfo.position));
                    Toast.makeText(this, "加入黑名单OK", Toast.LENGTH_SHORT).show();
                    getGroupInfo();
                }else{
                    Toast.makeText(this, "不是群主,不能被加入黑名单", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    //获取群信息
    public void getGroupInfo(){
//        //根据群组ID从本地获取群组基本信息
//        EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
        new Thread(new Runnable() {

            @Override
            public void run() {
                //根据群组ID从服务器获取群组基本信息
                try {
                    EMGroup group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);
                    membersList = group.getMembers();
                    groupName = group.getGroupName();
                    owner = group.getOwner();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvGroupName.setText(groupName);
                            adapter = new GroupListAdapter(GroupDetailsActivity.this,membersList);
                            groupGv.setAdapter(adapter);
                            if(EMClient.getInstance().getCurrentUser().equals(owner)){
                                bt_destroyGroup.setText("解散群组");
                                blockGroupMessage_ll.setVisibility(View.GONE);
                                changeGroupName.setVisibility(View.VISIBLE);
                                blockNameList.setVisibility(View.VISIBLE);
                            }else{
                                bt_destroyGroup.setText("退出群组");
                                blockGroupMessage_ll.setVisibility(View.VISIBLE);
                                changeGroupName.setVisibility(View.GONE);
                                blockNameList.setVisibility(View.GONE);
                            }
                        }
                    });
//

                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    //加人
    public void addgroup(final String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(owner.equals(EMClient.getInstance().getCurrentUser())){
                        //群主加人调用此方法
                        EMClient.getInstance().groupManager().addUsersToGroup(groupId, new String[]{name});
                    }else{
                        //私有群里，如果开放了群成员邀请，群成员邀请调用下面方法
                        EMClient.getInstance().groupManager().inviteUser(groupId, new String[]{name}, null);
                    }
                    handler.sendEmptyMessage(1);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //群主踢人
    public void removeUserGroup(final String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //把username从群组里删除
                try {
                    EMClient.getInstance().groupManager().removeUserFromGroup(groupId, name);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //将群成员拉入群组的黑名单
    public void addblackUser(final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().blockUser(groupId, username);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //删除对话及聊天记录
    public void deleteConversation(){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(groupId);
        conversation.clearAllMessages();
        Toast.makeText(this, "聊天记录已清空", Toast.LENGTH_SHORT).show();
    }

    //改变群名称
    public void changeGroupName(final String changegroupName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //groupId 需要改变名称的群组的id
                //changedGroupName 改变后的群组名称
                try {
                    EMClient.getInstance().groupManager().changeGroupName(groupId,changegroupName);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //解散群组
    public void destroyGroup(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().destroyGroup(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupDetailsActivity.this, groupId+"解散成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //退出群组
    public void leaveGroup(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupDetailsActivity.this, groupId+"退出成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //屏蔽群组
    public void blockGroupMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().blockGroupMessage(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupDetailsActivity.this, "屏蔽成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //取消屏蔽群组
    public void unBlockGroupMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().unblockGroupMessage(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupDetailsActivity.this, "取消屏蔽成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //弹出修改群名称的Dialog
    public void changeGroupNameDialog() {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("输入修改后的群名称");
        View view = this.getLayoutInflater().inflate(R.layout.dialog_communal, null);
        TextView textView = (TextView) view.findViewById(R.id.ml_dialog_text_message);
        final EditText editText = (EditText) view.findViewById(R.id.ml_dialog_edit_input);
        editText.setHint("输入内容不能为空");
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(GroupDetailsActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    changeGroupName(name);
                    getGroupInfo();
                    Toast.makeText(GroupDetailsActivity.this, name+"修改成功", Toast.LENGTH_SHORT).show();

                }

            }
        });
        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        createConversationDialog = alertDialogBuilder.create();
        createConversationDialog.show();
    }
}
