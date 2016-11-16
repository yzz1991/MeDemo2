package com.geri.app.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by Geri on 2016/11/4.
 */
public class ApplicationNoticeActivity extends AppCompatActivity{
    // 当前会话对象
    private EMConversation mConversation;
    private List<EMMessage> mMessages;
    private String groupTo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicationnotice);

        groupTo = getIntent().getStringExtra("groupId");
        initView();
    }

    private void initView() {
        TextView notice_back = (TextView) findViewById(R.id.notice_back);
        ListView lv = (ListView) findViewById(R.id.notice_lv);

        mConversation = EMClient.getInstance().chatManager().getConversation(groupTo, null, true);
        mMessages = mConversation.getAllMessages();
        lv.setAdapter(new NoticeAdapter());

        notice_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    class NoticeAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = LayoutInflater.from(ApplicationNoticeActivity.this);
            final EMMessage message = mMessages.get(position);
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_notice_lv,null);
            }
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            final Button agree = (Button) convertView.findViewById(R.id.agree);
            final Button refuse = (Button) convertView.findViewById(R.id.refuse);
            EMTextMessageBody body = (EMTextMessageBody) message.getBody();
            String messageStr = body.getMessage();
            tv_username.setText(messageStr);
            final String inviter = message.getStringAttribute("inviter", "");
            final String reason = message.getStringAttribute("reason", "");
            final String groupId = message.getStringAttribute("groupId", "");
            final String isfrom = message.getStringAttribute("isfrom", "");
            tv_content.setText(message.getStringAttribute("reason",""));
            if(message.getStringAttribute("status","").equals("已同意")){
                agree.setText("已同意");
                refuse.setText("拒绝");
            }else if(message.getStringAttribute("status","").equals("")){
                agree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isfrom.equals("invite")){
                            accept(groupId,inviter);
                        }else if(isfrom.equals("apply")){
                            acceptApply(inviter,groupId);
                        }
                        message.setAttribute("status","已同意");
                        agree.setText("已同意");
                    }
                });
                refuse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isfrom.equals("invite")){
                            refuse(groupId,inviter,reason);
                        }else if(isfrom.equals("apply")){
                            refuseApply(groupId,inviter,reason);
                        }

                        message.setAttribute("status","已拒绝");
                        refuse.setText("已拒绝");
                    }
                });
            }else if(message.getStringAttribute("status","").equals("已拒绝")){
                agree.setText("同意");
                refuse.setText("已拒绝");
            }else if(message.getStringAttribute("status","").equals("被同意") ||
                    message.getStringAttribute("status","").equals("被拒绝")){
                agree.setVisibility(View.INVISIBLE);
                refuse.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
    }
    //同意邀请
    public void accept(final String groupId,final String inviter){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().acceptInvitation(groupId,inviter);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //拒绝邀请
    public void refuse(final String groupId,final String inviter, final String reason){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().declineInvitation(groupId,inviter,reason);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //同意申请
    public void acceptApply(final String inviter,final String groupId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().acceptApplication(inviter,groupId);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //拒绝申请
    public void refuseApply(final String inviter,final String groupId, final String reason){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().declineApplication(inviter,groupId,reason);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
