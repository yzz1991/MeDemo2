package com.geri.app.demo.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

import static com.geri.app.ui.R.id.tv_username;
import static com.hyphenate.chat.EMMessage.Type.TXT;

/**
 * Created by Geri on 2016/11/17.
 */

public class ConversationAdapter extends BaseAdapter {
    private Context mContext;
    private List<EMConversation> list;
    private LayoutInflater mInflater;
    private String content;
    // 刷新会话列表
    private final int HANDLER_CONVERSATION_REFRESH = 0;
    private MyHandler mHandler;

    public ConversationAdapter(List<EMConversation> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        mHandler = new MyHandler();
    }

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
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_conversation,null);
            holder = new ViewHolder();
            holder.tvUserName = (TextView) convertView.findViewById(tv_username);
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        EMGroup group = EMClient.getInstance().groupManager().getGroup(list.get(position).conversationId());
        if(list.get(position).isGroup() && group != null){
            holder.tvUserName.setText(group.getGroupName());
        }else{
            holder.tvUserName.setText(list.get(position).getUserName());
        }
        if(list.get(position).getAllMessages().size() > 0){
            EMMessage lastMessage = list.get(position).getLastMessage();
            if(lastMessage.getType() == TXT){
                content = ((EMTextMessageBody) lastMessage.getBody()).getMessage();
            } else if(lastMessage.getType().equals("FILE")){
                content = "[" + "文件" + "]";
            }else if(lastMessage.getType().equals("IMAGE")){
                content = "[" + "图片" + "]";
            }else if(lastMessage.getType().equals("LOCATION")){
                content = "[" + "位置" + "]";
            }else if(lastMessage.getType().equals("VIDEO")){
                content = "[" + "视频" + "]";
            }else if(lastMessage.getType().equals("VIDEO")){
                content = "[" + "语音" + "]";
            }
            // 判断这条消息状态，如果失败加上失败前缀提示
            if(lastMessage.status() == EMMessage.Status.FAIL){
                content = "[" + "失败" + "]" + content;
            }
        }else{
            content = "空";
        }

        holder.tvContent.setText(content);
        return convertView;
    }

    class ViewHolder{
        TextView tvUserName;
        TextView tvContent;
    }

    /**
     * 供界面调用的刷新 Adapter 的方法
     */
    public void refreshList() {
        Message msg = mHandler.obtainMessage();
        msg.what = HANDLER_CONVERSATION_REFRESH;
        mHandler.sendMessage(msg);
    }

    /**
     * 自定义Handler，用来处理消息的刷新等
     */
    protected class MyHandler extends Handler {
        private void refresh() {
            notifyDataSetChanged();
        }

        @Override
        public void handleMessage(Message msg) {
            //            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_CONVERSATION_REFRESH:
                    refresh();
                    break;
            }
        }
    }

}
