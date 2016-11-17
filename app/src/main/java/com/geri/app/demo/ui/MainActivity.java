package com.geri.app.demo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.geri.app.demo.fragment.ContactsFragment;
import com.geri.app.demo.fragment.ConversationFragment;
import com.geri.app.demo.fragment.SettingFragment;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2016/10/18.
 */
public class MainActivity extends FragmentActivity {

    private EditText edUserName;
    private String chatId;
    private long mExitTime ; //退出时间

    private TextView mTextView;
    // 创建新会话对话框
    private AlertDialog createConversationDialog;
    private AlertDialog.Builder alertDialogBuilder;
    private ViewPager homeVp;
    private RadioButton rbConversation;
    private RadioButton rbContacts;
    private RadioButton rbSetting;
    private List<String> tagList = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 判断sdk是否登录成功过，并没有退出和被踢，否则跳转到登陆界面
        if (!EMClient.getInstance().isLoggedInBefore()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        initView();
    }

    private void initView() {
        homeVp = (ViewPager) findViewById(R.id.home_vp);
        RadioGroup rg = (RadioGroup) findViewById(R.id.rg);
        rbConversation = (RadioButton) findViewById(R.id.rb_conversation);
        rbContacts = (RadioButton) findViewById(R.id.rb_contacts);
        rbSetting = (RadioButton) findViewById(R.id.rb_setting);

        homeVp.setAdapter(new HomeViewPagerAdapter(getSupportFragmentManager()));
        homeVp.setCurrentItem(0);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_conversation:
                        homeVp.setCurrentItem(0);
                        break;

                    case R.id.rb_contacts:
                        homeVp.setCurrentItem(1);
                        break;

                    case R.id.rb_setting:
                        homeVp.setCurrentItem(2);
                        break;

                }
            }
        });

        homeVp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        rbConversation.setChecked(true);
                        rbContacts.setChecked(false);
                        rbSetting.setChecked(false);
                        break;
                    case 1:
                        rbContacts.setChecked(true);
                        rbConversation.setChecked(false);
                        rbSetting.setChecked(false);
                        break;
                    case 2:
                        rbSetting.setChecked(true);
                        rbConversation.setChecked(false);
                        rbContacts.setChecked(false);

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        edUserName = (EditText) findViewById(R.id.ed_userName);
//        mTextView = (TextView) findViewById(R.id.text_view);
//        Button bt_sessionPage = (Button) findViewById(R.id.bt_sessionPage);
//        Button bt_applyFriend = (Button) findViewById(R.id.bt_applyFriend);
//        Button bt_friendList = (Button) findViewById(R.id.bt_friendList);
//        Button bt_groupList = (Button) findViewById(R.id.bt_groupList);
//        Button btLogout = (Button) findViewById(R.id.bt_loginOut);
//        Button bt_createGroup = (Button) findViewById(R.id.bt_createGroup);
//        Button btSession = (Button) findViewById(R.id.bt_session);
//        Button bt_addGroup = (Button) findViewById(R.id.bt_addGroup);
//        bt_sessionPage.setOnClickListener(this);
//        bt_applyFriend.setOnClickListener(this);
//        bt_friendList.setOnClickListener(this);
//        bt_groupList.setOnClickListener(this);
//        bt_createGroup.setOnClickListener(this);
//        btLogout.setOnClickListener(this);
//        btSession.setOnClickListener(this);
//        bt_addGroup.setOnClickListener(this);

    }

    class HomeViewPagerAdapter extends FragmentPagerAdapter{
        FragmentManager mFragmentManager;

        public HomeViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = new ConversationFragment();
                    break;

                case 1:
                    fragment = new ContactsFragment();
                    break;

                case 2:
                    fragment = new SettingFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            tagList.add(makeFragmentName(container.getId(), getItemId(position))); //把tag存起来
//            return super.instantiateItem(container, position);
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object){
//            super.destroyItem(container, position, object);
//            tagList.remove(makeFragmentName(container.getId(), getItemId(position)));//把tag删掉
//        }
//
//        public void update(){
//            notifyDataSetChanged();//并不能起到更新Fragment内容的作用。
//        }
//
//        public void update(int position){//这个事真正的更新Fragment的内容
//            Fragment fragment = mFragmentManager.findFragmentByTag(tagList.get(position));
//            if(fragment == null){
//                return;
//            }
//        }
//
//        public String makeFragmentName(int viewId, long id) {
//            return "android:switcher:" + viewId + ":" + id;
//        }

    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            //跳转聊天页面
//            case R.id.bt_sessionPage:
//                startActivity(new Intent(this, ConversationActivity.class));
//                break;
//            //申请好友
//            case R.id.bt_applyFriend:
//                addFriend();
//                break;
//            //获取好友列表
//            case R.id.bt_friendList:
//                getFriendList();
//                break;
//            //群组列表
//            case R.id.bt_groupList:
//                startActivity(new Intent(this, GroupListActivity.class));
//                break;
//            //创建群组
//            case bt_createGroup:
//                changeGroupNameDialog();
//                break;
//            //新会话
//            case R.id.bt_session:
//                newSessionDialog();
//                break;
//            //退出登录
//            case R.id.bt_loginOut:
//                logout();
//                break;
//            //申请加群
//            case R.id.bt_addGroup:
//                addGroup();
//                break;
//
//        }
//    }
//
//    //申请加好友
//    public void addFriend() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 获取我们发起聊天的者的username
//                chatId = edUserName.getText().toString().trim();
//                if (!TextUtils.isEmpty(chatId)) {
//                    //获取当前登录用户的username
//                    String currUsername = EMClient.getInstance().getCurrentUser();
//                    if (chatId.equals(currUsername)) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "不能加自己为好友", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        //参数为要添加的好友的username和添加理由
//                        try {
//                            EMClient.getInstance().contactManager().addContact(chatId, "交个朋友吧");
////                    Log.i("addFriend", "加好友");
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(MainActivity.this, chatId + "申请加好友", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        } catch (HyphenateException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }).start();
//    }
//
////    //同意好友申请
////    public void agreeFriend() {
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////                try {
////                    EMClient.getInstance().contactManager().acceptInvitation(chatId);
//////            Log.i("agreeFriend", "同意好友申请了");
////                    runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            Toast.makeText(MainActivity.this, "同意好友申请了", Toast.LENGTH_SHORT).show();
////                        }
////                    });
////
////                } catch (HyphenateException e) {
////                    e.printStackTrace();
////                }
////            }
////        }).start();
////
////    }
//
//    //获取好友列表
//    public void getFriendList() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "Get Contact success!", Toast.LENGTH_LONG).show();
//                            mTextView.setText("Get Contact success \n");
//                            for (int i = 0; i < usernames.size(); i++) {
//                                mTextView.setText(mTextView.getText() + usernames.get(i) + "\n");
//                            }
//                        }
//                    });
//                } catch (HyphenateException e) {
//                    e.printStackTrace();
//                    int errorCode = e.getErrorCode();
//                    switch (errorCode) {
//
//                    }
//                }
//            }
//        }).start();
//
//
//    }
//
//    //退出登录
//    private void logout() {
//        EMClient.getInstance().logout(true, new EMCallBack() {
//
//            @Override
//            public void onSuccess() {
//                finish();
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            }
//
//            @Override
//            public void onProgress(int progress, String status) {
//
//            }
//
//            @Override
//            public void onError(int code, String error) {
//
//            }
//        });
//    }
//
//    //创建群组
//    private void createGroup(final String name) {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                EMGroupManager.EMGroupOptions option = new EMGroupManager.EMGroupOptions();
//                option.maxUsers = 200;
//                option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
//                try {
//                    final EMGroup group = EMClient.getInstance().groupManager().createGroup(name,
//                            "哈哈哈哈哈哈", new String[]{""}, "测试", option);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//                            intent.putExtra("ec_chat_id", group.getGroupId());
//                            intent.putExtra("type", EMConversation.EMConversationType.GroupChat);
//                            startActivity(intent);
//                            Toast.makeText(MainActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } catch (HyphenateException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    //弹出建群名称的Dialog
//    public void changeGroupNameDialog() {
//        alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setTitle("创建群组");
//        View view = this.getLayoutInflater().inflate(R.layout.dialog_communal, null);
//        TextView textView = (TextView) view.findViewById(R.id.ml_dialog_text_message);
//        final EditText editText = (EditText) view.findViewById(R.id.ml_dialog_edit_input);
//        editText.setHint("输入内容不能为空");
//        alertDialogBuilder.setView(view);
//        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String name = editText.getText().toString().trim();
//                if (TextUtils.isEmpty(name)) {
//                    Toast.makeText(MainActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    createGroup(name);
//                }
//
//            }
//        });
//        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        createConversationDialog = alertDialogBuilder.create();
//        createConversationDialog.show();
//    }
//
//    //发起聊天
//    private void session(String name) {
//        if (!TextUtils.isEmpty(name)) {
//            // 获取当前登录用户的 username
//            String currUsername = EMClient.getInstance().getCurrentUser();
//            if (name.equals(currUsername)) {
//                Toast.makeText(MainActivity.this, "不能和自己聊天", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // 跳转到聊天界面，开始聊天
//            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//            intent.putExtra("ec_chat_id", name);
//            intent.putExtra("type", EMConversation.EMConversationType.Chat);
//            startActivity(intent);
//        } else {
//            Toast.makeText(MainActivity.this, "Username 不能为空", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    //申请加群
//    public void addGroup() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 获取我们发起聊天的者的username
//                chatId = edUserName.getText().toString().trim();
//                if (!TextUtils.isEmpty(chatId)) {
//                    //获取当前登录用户的username
//                    final String currUsername = EMClient.getInstance().getCurrentUser();
//                    if (chatId.equals(currUsername)) {
//                    } else {
//                        //参数为要添加的好友的username和添加理由
//                        try {
//                            EMClient.getInstance().groupManager().applyJoinToGroup(chatId,"加入群");
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(MainActivity.this, currUsername + "申请加群", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        } catch (HyphenateException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }).start();
//    }
//
//    //弹出新会话的Dialog
//    public void newSessionDialog() {
//        alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setTitle("输入用户名称");
//        View view = this.getLayoutInflater().inflate(R.layout.dialog_communal, null);
//        TextView textView = (TextView) view.findViewById(R.id.ml_dialog_text_message);
//        final EditText editText = (EditText) view.findViewById(R.id.ml_dialog_edit_input);
//        editText.setHint("输入内容不能为空");
//        alertDialogBuilder.setView(view);
//        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String name = editText.getText().toString().trim();
//                if (TextUtils.isEmpty(name)) {
//                    Toast.makeText(MainActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    session(name);
//                }
//
//            }
//        });
//        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        createConversationDialog = alertDialogBuilder.create();
//        createConversationDialog.show();
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch(keyCode)
//        {
//
//            case KeyEvent.KEYCODE_BACK:
//
//                long secondTime = System.currentTimeMillis();
//                if (secondTime - mExitTime > 2000) { //如果两次按键时间间隔大于2秒，则不退出
//                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                    mExitTime = secondTime;//更新mExitTime
//                    return true;
//                } else {
//                    System.exit(0);
//                }
//                break;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    //    private void initView() {
//
//        vp = (ViewPager) findViewById(R.id.vp);
//        mTabs = new Button[3];
//        mTabs[0] = (Button) findViewById(R.id.bt_conversation);
//        mTabs[1] = (Button) findViewById(R.id.bt_address_list);
//        mTabs[2] = (Button) findViewById(R.id.bt_setting);
//        // 默认选中第一个tab
//        mTabs[0].setSelected(true);
//
//        sessionFragment = new SessionFragment();
//        contactsFragment = new ContactsFragment();
//        settingsFragment = new SettingsFragment();
//        fragments = new Fragment[]{sessionFragment, contactsFragment, settingsFragment};
//        vp.setAdapter(new VpAdapter(getSupportFragmentManager()));
//        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//
//    }
//
//    class VpAdapter extends FragmentPagerAdapter{
//
//        public VpAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return fragments[position];
//        }
//
//        @Override
//        public int getCount() {
//            return fragments.length;
//        }
//    }
//
//    public void onTabClicked(View view) {
//        switch (view.getId()){
//            case R.id.bt_conversation:
//                index = 0;
//                vp.setCurrentItem(0);
//                break;
//
//            case R.id.bt_address_list:
//                index = 1;
//                vp.setCurrentItem(1);
//                break;
//
//            case R.id.bt_setting:
//                index = 2;
//                vp.setCurrentItem(2);
//                break;
//        }
//    }
}
