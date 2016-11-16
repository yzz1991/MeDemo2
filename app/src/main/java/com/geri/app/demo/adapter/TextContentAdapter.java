package com.geri.app.demo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geri.app.demo.MLConstants;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;

import java.io.IOException;
import java.util.List;

/**
 * Created by Geri on 2016/10/23.
 */
public class TextContentAdapter extends BaseAdapter {

    private Context context;
    // 聊天对象的 useranme/groupid
    private String chatId;
    // 当前会话对象
    private EMConversation mConversation;
    private List<EMMessage> mMessages;
    // item 类型
    protected int mViewType;
    private LayoutInflater mLayoutinflater;

    public TextContentAdapter(Context context, String chatId) {
        this.context = context;
        this.chatId = chatId;
        mLayoutinflater = LayoutInflater.from(context);

        /**
         * 初始化会话对象，这里有三个参数么，
         * mChatid 第一个表示会话的当前聊天的 useranme 或者 groupid
         * null 第二个是会话类型可以为空
         * true 第三个表示如果会话不存在是否创建
         */
        mConversation = EMClient.getInstance().chatManager().getConversation(chatId, null, true);
        mMessages = mConversation.getAllMessages();
    }

    public void refresh() {
        mMessages.clear();
        mMessages.addAll(mConversation.getAllMessages());
    }

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
    public int getItemViewType(int position) {
        EMMessage message = mMessages.get(position);
        int itemType = -1;
            switch (message.getType()) {
                case TXT:
                    // 文本消息
                    itemType = message.direct() == EMMessage.Direct.SEND ? MLConstants.MSG_TYPE_TEXT_SEND : MLConstants.MSG_TYPE_TEXT_RECEIVED;
                    break;
                case IMAGE:
                    // 图片消息
                    itemType = message.direct() == EMMessage.Direct.SEND ? MLConstants.MSG_TYPE_IMAGE_SEND : MLConstants.MSG_TYPE_IMAGE_RECEIVED;
                    break;
                case VOICE:
                    // 语音消息
                    itemType = message.direct() == EMMessage.Direct.SEND ? MLConstants.MSG_TYPE_VOICE_SEND : MLConstants.MSG_TYPE_VOICE_RECEIVED;
                    break;
                case LOCATION:
                    // 位置消息
                    itemType = message.direct() == EMMessage.Direct.SEND ? MLConstants.MSG_TYPE_LOCATION_SEND : MLConstants.MSG_TYPE_LOCATION_RECEIVED;
                    break;
                default:
                    // 默认返回txt类型
                    itemType = message.direct() == EMMessage.Direct.SEND ? MLConstants.MSG_TYPE_TEXT_SEND : MLConstants.MSG_TYPE_TEXT_RECEIVED;
                    break;

        }
        return itemType;
    }

//    @Override
//    public int getViewTypeCount() {
//        return 4;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EMMessage message = mMessages.get(position);
        ViewHolder1 holder1 = null;
        ViewHolder2 holder2 = null;
        ViewHolder3 holder3 = null;
        ViewHolder4 holder4 = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                //文本收发
                case MLConstants.MSG_TYPE_TEXT_SEND:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_text_send, null);
                    holder1 = new ViewHolder1();
                    holder1.txt_content_send = (TextView) convertView.findViewById(R.id.txt_content_send);
                    convertView.setTag(holder1);
                    break;
                case MLConstants.MSG_TYPE_TEXT_RECEIVED:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_text_received, null);
                    holder1 = new ViewHolder1();
                    holder1.tv_nickName = (TextView) convertView.findViewById(R.id.tv_nickName);
                    holder1.txt_content_received = (TextView) convertView.findViewById(R.id.txt_content_received);
                    convertView.setTag(holder1);
                    break;

                //图片收发
                case MLConstants.MSG_TYPE_IMAGE_SEND:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_image_send, null);
                    holder2 = new ViewHolder2();
                    holder2.img_content_send = (ImageView) convertView.findViewById(R.id.img_content_send);
                    convertView.setTag(holder2);
                    break;
                case MLConstants.MSG_TYPE_IMAGE_RECEIVED:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_image_received, null);
                    holder2 = new ViewHolder2();
                    holder2.img_tv_nickName = (TextView) convertView.findViewById(R.id.img_tv_nickName);
                    holder2.img_content_received = (ImageView) convertView.findViewById(R.id.img_content_received);
                    convertView.setTag(holder2);
                    break;

                //语音收发
                case MLConstants.MSG_TYPE_VOICE_SEND:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_voice_send, null);
                    holder3 = new ViewHolder3();
                    holder3.voice_content_send = (RelativeLayout) convertView.findViewById(R.id.voice_content_send);
                    convertView.setTag(holder3);
                    break;
                case MLConstants.MSG_TYPE_VOICE_RECEIVED:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_voice_received, null);
                    holder3 = new ViewHolder3();
                    holder3.voice_tv_nickName = (TextView) convertView.findViewById(R.id.voice_tv_nickName);
                    holder3.voice_content_received = (RelativeLayout) convertView.findViewById(R.id.voice_content_received);
                    convertView.setTag(holder3);
                    break;

                //位置收发
                case MLConstants.MSG_TYPE_LOCATION_SEND:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_location_send, null);
                    holder4 = new ViewHolder4();
                    holder4.location_content_send = (TextView) convertView.findViewById(R.id.location_content_send);
                    convertView.setTag(holder4);
                    break;
                case MLConstants.MSG_TYPE_LOCATION_RECEIVED:
                    convertView = mLayoutinflater.inflate(R.layout.item_msg_location_received, null);
                    holder4 = new ViewHolder4();
                    holder4.location_tv_nickName = (TextView) convertView.findViewById(R.id.location_tv_nickName);
                    holder4.location_content_received = (TextView) convertView.findViewById(R.id.location_content_received);
                    convertView.setTag(holder4);
                    break;
            }
        } else {
            switch (type) {
                case MLConstants.MSG_TYPE_TEXT_SEND:
                case MLConstants.MSG_TYPE_TEXT_RECEIVED:
                    holder1 = (ViewHolder1) convertView.getTag();
                    break;

                case MLConstants.MSG_TYPE_IMAGE_SEND:
                case MLConstants.MSG_TYPE_IMAGE_RECEIVED:
                    holder2 = (ViewHolder2) convertView.getTag();
                    break;

                // 正常的语音消息
                case MLConstants.MSG_TYPE_VOICE_SEND:
                case MLConstants.MSG_TYPE_VOICE_RECEIVED:
                    holder3 = (ViewHolder3) convertView.getTag();
                    break;

                // 位置的消息
                case MLConstants.MSG_TYPE_LOCATION_SEND:
                case MLConstants.MSG_TYPE_LOCATION_RECEIVED:
                    holder4 = (ViewHolder4) convertView.getTag();
                    break;
            }
        }
        //设置资源
        switch (type){
            case MLConstants.MSG_TYPE_TEXT_SEND:
            case MLConstants.MSG_TYPE_TEXT_RECEIVED:
                EMTextMessageBody body = (EMTextMessageBody) message.getBody();
                Log.d("message",body.toString());
                String messageStr = body.getMessage().toString();
                if(!TextUtils.isEmpty(messageStr)) {
                    if(message.direct() == EMMessage.Direct.SEND){
                        holder1.txt_content_send.setText(messageStr);
                    }else{
                        if(message.getChatType() == EMMessage.ChatType.Chat){
                            holder1.tv_nickName.setVisibility(View.GONE);
                        }else{
                            holder1.tv_nickName.setVisibility(View.VISIBLE);
                            holder1.tv_nickName.setText(message.getFrom());
                        }
                        holder1.txt_content_received.setText(messageStr);
                    }
                }
                break;

            case MLConstants.MSG_TYPE_IMAGE_SEND:
            case MLConstants.MSG_TYPE_IMAGE_RECEIVED:
                // 获取图片消息体
                EMImageMessageBody imgBody = (EMImageMessageBody) message.getBody();
                String originalPath = imgBody.getLocalUrl();
                String thumbnailsPath = imgBody.thumbnailLocalPath();
                if( message.direct() == EMMessage.Direct.SEND){
                    Bitmap bm = BitmapFactory.decodeFile(originalPath);
                    holder2.img_content_send.setImageBitmap(bm);
                }else{
                    if(message.getChatType() == EMMessage.ChatType.Chat){
                        holder2.img_tv_nickName.setVisibility(View.GONE);
                    }else{
                        holder2.img_tv_nickName.setVisibility(View.VISIBLE);
                        holder2.img_tv_nickName.setText(message.getFrom());

                    }
                    Bitmap bm = BitmapFactory.decodeFile(thumbnailsPath);
                    holder2.img_content_received.setImageBitmap(bm);
                }
                break;

            // 正常的语音消息
            case MLConstants.MSG_TYPE_VOICE_SEND:
            case MLConstants.MSG_TYPE_VOICE_RECEIVED:
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                // 获取语音消息体
                EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
                try {
                    mediaPlayer.setDataSource(voiceBody.getLocalUrl());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if( message.direct() == EMMessage.Direct.SEND){
                    holder3.voice_content_send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayer.start();
                        }
                    });
                }else{
                    if(message.getChatType() == EMMessage.ChatType.Chat){
                        holder3.voice_tv_nickName.setVisibility(View.GONE);
                    }else{
                        holder3.voice_tv_nickName.setVisibility(View.VISIBLE);
                        holder3.voice_tv_nickName.setText(message.getFrom());
                    }
                    holder3.voice_content_received.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayer.start();
                        }
                    });
                }
                break;

            // 位置的消息
            case MLConstants.MSG_TYPE_LOCATION_SEND:
            case MLConstants.MSG_TYPE_LOCATION_RECEIVED:
                // 获取位置消息体
                EMLocationMessageBody locationBody = (EMLocationMessageBody) message.getBody();
                String address = locationBody.getAddress();
                if( message.direct() == EMMessage.Direct.SEND){
                    holder4.location_content_send.setText(address);
                }else{
                    if(message.getChatType() == EMMessage.ChatType.Chat){
                        holder4.location_tv_nickName.setVisibility(View.GONE);
                    }else{
                        holder4.location_tv_nickName.setVisibility(View.VISIBLE);
                        holder4.location_tv_nickName.setText(message.getFrom());
                    }
                    holder4.location_content_received.setText(address);
                }
                break;
        }
        return convertView;
    }

    class ViewHolder1{
        TextView tv_nickName;
        TextView txt_content_received;
        TextView txt_content_send;
    }
    class ViewHolder2{
        TextView  img_tv_nickName;
        ImageView img_content_received;
        ImageView img_content_send;
    }
    class ViewHolder3{
        TextView  voice_tv_nickName;
        RelativeLayout voice_content_received;
        RelativeLayout voice_content_send;
    }
    class ViewHolder4{
        TextView  location_tv_nickName;
        TextView location_content_received;
        TextView location_content_send;
    }

}
