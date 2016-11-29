package com.geri.app.demo.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.geri.app.demo.adapter.TextContentAdapter;
import com.geri.app.demo.application.MyHyphenate;
import com.geri.app.demo.ui.call.VoiceCallActivity;
import com.geri.app.demo.utils.MLConstants;
import com.geri.app.demo.utils.MLMessageUtils;
import com.geri.app.demo.utils.MLNotifier;
import com.geri.app.demo.utils.MLRecorder;
import com.geri.app.ui.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;

import java.io.File;
import java.util.List;

import static com.geri.app.ui.R.id.bt_send;

/**
 * Created by Geri on 2016/10/20.
 */

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String chatId;
    private EditText ed_msg_input;
    // 是否发送原图
    private boolean isOrigin = true;
    private LinearLayout yuyin, biaoqing, tupian, weizhi,call;
    // 对话框
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog photoModeDialog;
    private Uri mCameraImageUri = null;
    private File saveFile;
    private boolean flag;
    // 录制开始时间
    protected long startTime = 0L;
    // 录制持续时间
    protected int recordTime = 0;
    private ImageView yuyin_img;
    private ListView text_content_lv;
    private TextContentAdapter adapter;
    private TextView chat_back;
    private EMConversation.EMConversationType mConversationType;
    private EMConversation conversation;
    private String id;
    private String userName;
    private EMMessageListener mMessageListener;
    private TextView tv_username;
    private ImageView tv_groupMore;
    private EMGroup group;
    private TextView tv_more;
    private PopupWindow mPopWindow;
    // 进度对话框
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("ec_chat_id");
        mConversationType = (EMConversation.EMConversationType) getIntent().getExtras().get("type");
        conversation = EMClient.getInstance().chatManager().getConversation(chatId, mConversationType, true);
        id = conversation.conversationId();
        userName = conversation.getUserName();

        initView();

    }

    private void initView() {
        tv_username = (TextView) findViewById(R.id.tv_username);
        ed_msg_input = (EditText) findViewById(R.id.ec_edit_message_input);
        text_content_lv = (ListView) findViewById(R.id.text_content_lv);
        tv_groupMore = (ImageView) findViewById(R.id.tv_groupMore);
        yuyin = (LinearLayout) findViewById(R.id.yuyin);
        yuyin_img = (ImageView) findViewById(R.id.yuyin_img);
        biaoqing = (LinearLayout) findViewById(R.id.biaoqing);
        tupian = (LinearLayout) findViewById(R.id.tupian);
        weizhi = (LinearLayout) findViewById(R.id.weizhi);
        call = (LinearLayout) findViewById(R.id.call);
        chat_back = (TextView) findViewById(R.id.chat_back);
        Button bt_send = (Button) findViewById(R.id.bt_send);
        tv_more = (TextView) findViewById(R.id.tv_more);
//        TextView deleteBlock = (TextView) findViewById(R.id.deleteBlock);
        if(conversation.getType().equals(EMConversation.EMConversationType.GroupChat)){
            tv_groupMore.setVisibility(View.VISIBLE);
            tv_more.setVisibility(View.GONE);
            group = EMClient.getInstance().groupManager().getGroup(conversation.conversationId());
            if(conversation.isGroup() && group != null){
                tv_username.setText(group.getGroupName());
            }
        }else{
            tv_username.setText(userName);
            tv_more.setVisibility(View.VISIBLE);
            tv_groupMore.setVisibility(View.GONE);
        }

        if(mConversationType.equals(EMConversation.EMConversationType.Chat)){
            call.setVisibility(View.VISIBLE);
        }else {
            call.setVisibility(View.GONE);
        }
//        this.registerForContextMenu(call);

        bt_send.setOnClickListener(this);
        tv_groupMore.setOnClickListener(this);
        yuyin.setOnClickListener(this);
        biaoqing.setOnClickListener(this);
        tupian.setOnClickListener(this);
        weizhi.setOnClickListener(this);
        chat_back.setOnClickListener(this);
        tv_more.setOnClickListener(this);
//        deleteBlock.setOnClickListener(this);

        adapter = new TextContentAdapter(this, chatId);
        text_content_lv.setAdapter(adapter);
        setMessageListener();
        this.registerForContextMenu(text_content_lv);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case bt_send:
                String content = ed_msg_input.getText().toString().trim();
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(this, "输入内容为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendTextMessage(content);
                adapter.refresh();
                adapter.notifyDataSetChanged();
                break;

            case R.id.yuyin:
                if (!flag) {
                    startRecord(null);
                    yuyin_img.setImageResource(R.drawable.yuyin_selected);
                    flag = true;
                } else {
                    stopRecord();
                    yuyin_img.setImageResource(R.drawable.yuyin);
                    flag = false;
                    adapter.refresh();
                    adapter.notifyDataSetChanged();
                }
                break;

            case R.id.biaoqing:

                break;

            case R.id.tupian:
                // 弹出选择图片方式对话框
                selectPhotoMode();
                break;

            case R.id.weizhi:
                Intent intent = new Intent(ChatActivity.this, MapShowActivity.class);
                startActivityForResult(intent, MLConstants.ML_REQUEST_CODE_LOCATION);
                break;
            //退回会话页面
            case R.id.chat_back:
                Intent intent2 = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent2);
                finish();
                break;

            case R.id.tv_groupMore:
                if(conversation.getType().equals(EMConversation.EMConversationType.GroupChat)){
                    Intent intent1 = new Intent(ChatActivity.this, GroupDetailsActivity.class);
                    intent1.putExtra("groupId", id);
                    startActivity(intent1);
                }else{
                    Toast.makeText(this, "不是群组，不能跳转", Toast.LENGTH_SHORT).show();

                }

                break;
            //更多
            case R.id.tv_more:
                addPopupWindow();
                break;
            //清空聊天记录
            case R.id.clearAllContext:
                clearAllContext();
                adapter.refresh();
                adapter.notifyDataSetChanged();
                mPopWindow.dismiss();
                break;
            //加入黑名单
            case R.id.addBlock:
                addBlockUser();
                mPopWindow.dismiss();
                break;
//
//            //从黑名单移除联系人
//            case R.id.deleteBlock:
//                deleteBlockUser();
//                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, Menu.NONE, "删除消息");
        menu.add(0, 0, Menu.NONE, "撤回");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        List<EMMessage> allMessages = conversation.getAllMessages();
        switch (item.getItemId()) {
            //删除消息
            case 0:
                conversation.removeMessage(allMessages.get(menuInfo.position).getMsgId());
                adapter.refresh();
                adapter.notifyDataSetChanged();
                break;
            //撤回消息
            case 1:
                recallMessage(allMessages.get(menuInfo.position));
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    //头部更多功能弹出框
    private void addPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.popu_char_more, null);
        int h = this.getWindowManager().getDefaultDisplay().getHeight();
        int w = this.getWindowManager().getDefaultDisplay().getWidth();
        mPopWindow = new PopupWindow(contentView);
        // 设置SelectPicPopupWindow的View
        mPopWindow.setContentView(contentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        mPopWindow.setWidth(w / 3 );
        // 设置SelectPicPopupWindow弹出窗体的高
        mPopWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(true);
        // 刷新状态
        mPopWindow.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        mPopWindow.setBackgroundDrawable(dw);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        mPopWindow.setAnimationStyle(R.style.AnimationPreview);
        LinearLayout addTaskLayout = (LinearLayout) contentView
                .findViewById(R.id.clearAllContext);
        LinearLayout teamMemberLayout = (LinearLayout) contentView
                .findViewById(R.id.addBlock);
        addTaskLayout.setOnClickListener(this);
        teamMemberLayout.setOnClickListener(this);
        showPopupWindow(tv_more);
    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!mPopWindow.isShowing()) {
            // 以下拉方式显示popupwindow
            mPopWindow.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
        } else {
            mPopWindow.dismiss();
        }
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v,
//                                    ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.add(0, 0, Menu.NONE, "语音通话");
//        menu.add(0, 1, Menu.NONE, "视频通话");
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case 0:
//                if(mConversationType.equals(EMConversation.EMConversationType.Chat)){
                    Intent intent = new Intent(ChatActivity.this, VoiceCallActivity.class);
//                    intent.putExtra("username", userName);
//                    intent.putExtra("isComingCall", false);
//                    startActivity(intent);
//                }
//                break;
//            case 1:
//                if(mConversationType.equals(EMConversation.EMConversationType.Chat)){
//                    Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
//                    intent.putExtra("username", userName);
//                    intent.putExtra("isComingCall", false);
//                    startActivity(intent);
//                }
//                break;
//            default:
//                return super.onContextItemSelected(item);
//        }
//        return true;
//    }

    /**
     * 撤回消息，将已经发送成功的消息进行撤回
     *
     * @param message 需要撤回的消息
     */
    private void recallMessage(final EMMessage message) {
        // 显示撤回消息操作的 dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在撤回 请稍候……");
        progressDialog.show();
        MLMessageUtils.sendRecallMessage(message, new EMCallBack() {
            @Override public void onSuccess() {
                // 关闭进度对话框
                progressDialog.dismiss();
                // 设置扩展为撤回消息类型，是为了区分消息的显示
                message.setAttribute(MLConstants.ML_ATTR_RECALL, true);
                // 更新消息
                EMClient.getInstance().chatManager().updateMessage(message);
                adapter.refresh();
                adapter.notifyDataSetChanged();
            }

            /**
             * 撤回消息失败
             * @param i 失败的错误码
             * @param s 失败的错误信息
             */
            @Override public void onError(final int i, final String s) {
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        // 弹出错误提示
                        if (s.equals(MLConstants.ML_ERROR_S_RECALL_TIME)) {
                            Toast.makeText(ChatActivity.this, "消息已经超过五分钟，不能撤回", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "撤回失败"+i + "-" + s, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 发送文本消息
     */
    private void sendTextMessage(String content) {

        ed_msg_input.setText("");
        // 创建一条文本消息
        EMMessage textMessage = EMMessage.createTxtSendMessage(content, chatId);
        sendMessage(textMessage);
    }


    //发送图片消息
    private void sendImageMessage(String path) {
        /**
         * 根据图片路径创建一条图片消息，需要三个参数，
         * path     图片路径
         * isOrigin 是否发送原图
         * mChatId  接收者
         */
        EMMessage imgMessage = EMMessage.createImageSendMessage(path, isOrigin, chatId);
        sendMessage(imgMessage);
    }

    /**
     * 发送语音消息
     *
     * @param path 语音文件的路径
     */
    private void sendVoiceMessage(String path, int time) {
        EMMessage voiceMessage = EMMessage.createVoiceSendMessage(path, time, chatId);
        sendMessage(voiceMessage);

    }

    /**
     * 发送位置信息
     */
    private void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        //latitude为纬度，longitude为经度，locationAddress为具体位置内容
        EMMessage locationmessage = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, chatId);
        sendMessage(locationmessage);

    }


    /**
     * 最终调用发送信息方法
     *
     * @param message 需要发送的消息
     */
    private void sendMessage(final EMMessage message) {

        // 设置不同的会话类型
        if (mConversationType == EMConversation.EMConversationType.Chat) {
            message.setChatType(EMMessage.ChatType.Chat);
        } else if (mConversationType == EMConversation.EMConversationType.GroupChat) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        } else if (mConversationType == EMConversation.EMConversationType.ChatRoom) {
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        /**
         *  调用sdk的消息发送方法发送消息，发送消息时要尽早的设置消息监听，防止消息状态已经回调，
         *  但是自己没有注册监听，导致检测不到消息状态的变化
         *  所以这里在发送之前先设置消息的状态回调
         */
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i("message", "发送成功");
            }

            @Override
            public void onError(final int i, final String s) {
                Log.i("message", "发送失败");
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 弹出选择图片发方式，是使用相机还是图库
     */
    private void selectPhotoMode() {
        String[] menus = {"相机", "图库"};
        if (alertDialogBuilder == null) {
            alertDialogBuilder = new AlertDialog.Builder(this);
        }
        // 设置弹出框 title
        //        alertDialogBuilder.setTitle(mActivity.getString(R.string.ml_dialog_title_select_photo_mode));
        // 设置弹出框的菜单项及点击事件
        alertDialogBuilder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 打开相机直接拍照
                        openCamera();
                        break;
                    case 1:
                        // 打开图库选择图片
                        openGallery();
                        break;
                    default:
                        openGallery();
                        break;
                }
            }
        });
        photoModeDialog = alertDialogBuilder.create();
        photoModeDialog.show();
    }

    /**
     * 打开相机去拍摄图片发送
     */
    private void openCamera() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        //告诉照相机应该保存的路径
        //获取外部存储路径
        File file = Environment.getExternalStorageDirectory();//  /mnt/sdcard
        saveFile = new File(file, "a.jpg");
        // 根据文件路径解析成Uri
        mCameraImageUri = Uri.fromFile(saveFile);
        //将保存路径传递
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);

        // 根据 Intent 启动一个带有返回值的 Activity，这里启动的就是相机，返回选择图片的地址
        this.startActivityForResult(intent, MLConstants.ML_REQUEST_CODE_CAMERA);
    }

    /**
     * 打开系统图库，去进行选择图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(intent, MLConstants.ML_REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case MLConstants.ML_REQUEST_CODE_CAMERA:
                // 相机拍摄的图片
                sendImageMessage(mCameraImageUri.getPath());
                adapter.refresh();
                adapter.notifyDataSetChanged();
                break;
            case MLConstants.ML_REQUEST_CODE_GALLERY:
                // 图库选择的图片，选择图片后返回获取返回的图片路径，然后发送图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumns = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    String image = c.getString(columnIndex);
                    c.close();
                    sendImageMessage(image);
                    adapter.refresh();
                    adapter.notifyDataSetChanged();
                }
                break;

            case MLConstants.ML_REQUEST_CODE_LOCATION:
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("addrStr");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                    adapter.refresh();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ChatActivity.this, "unable_to_get_loaction", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 开始录制
     */
    public void startRecord(String path) {
        // 调用录音机开始录制音频
        int recordError = MLRecorder.getInstance().startRecordVoice(path);
        if (recordError == MLRecorder.ERROR_NONE) {
            // 开始录音
            // 初始化开始录制时间
            startTime = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (MLRecorder.getInstance().isRecording()) {
                        // 睡眠 100 毫秒，
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        recordTime = (int) (System.currentTimeMillis() - startTime);
                    }
                }
            }).start();
        } else if (recordError == MLRecorder.ERROR_RECORDING) {
            // 录音进行中
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        // 调用录音机停止录制
        int recordError = MLRecorder.getInstance().stopRecordVoice();
        // 计算录制时间
        recordTime = (int) (System.currentTimeMillis() - startTime);
        if (recordTime < 1000) {
            // 录制时间太短
            Toast.makeText(this, "语音时间太短", Toast.LENGTH_SHORT).show();
        } else if (recordError == MLRecorder.ERROR_NONE) {
            // 录音成功
            sendVoiceMessage(MLRecorder.getInstance().getRecordFilePath(), recordTime);

        } else if (recordError == MLRecorder.ERROR_FAILED) {
            // 录音失败
            Toast.makeText(this, "录音失败:" + recordError, Toast.LENGTH_SHORT).show();
        } else if (recordError == MLRecorder.ERROR_SYSTEM) {
            // 录音失败，系统错误
            Toast.makeText(this, "录音失败，系统错误:" + recordError, Toast.LENGTH_SHORT).show();
        }

        recordTime = 0;
    }

    //清空聊天记录
    public void clearAllContext(){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(id);
        conversation.clearAllMessages();
        Toast.makeText(this, "聊天记录已清空", Toast.LENGTH_SHORT).show();
    }

    //联系人加入黑名单
    public void addBlockUser(){
        try {
            EMClient.getInstance().contactManager().addUserToBlackList(userName,false);
            Toast.makeText(this, "加入黑名单成功", Toast.LENGTH_SHORT).show();
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(conversation.getType().equals(EMConversation.EMConversationType.GroupChat)){
            group = EMClient.getInstance().groupManager().getGroup(conversation.conversationId());
            if(conversation.isGroup() && group != null){
                tv_username.setText(group.getGroupName());
            }
        }
        adapter.refresh();
        adapter.notifyDataSetChanged();
    }

    //消息监听
    private void setMessageListener() {
        mMessageListener = new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> list) {
                boolean isNotify = false;
                // 循环遍历当前收到的消息
                for (EMMessage message : list) {
                    String username = "";
                    if (mConversationType == EMConversation.EMConversationType.Chat) {
                        username = message.getFrom();
                    } else {
                        username = message.getTo();
                    }
                    // 判断消息是否是当前会话的消息
                    if (chatId.equals(username)) {
                        adapter.refresh();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } else {
                        isNotify = true;
                    }
                }
                if (isNotify) {
                    // 如果消息不是当前会话的消息发送通知栏通知
                    MLNotifier.getInstance().sendNotificationMessageList(list);
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> list) {
                if(MyHyphenate.getInstance().getActivityList().size()>0){
                    if(MyHyphenate.getInstance().getTopActivity().getClass().getSimpleName().equals("ChatActivity")){
                        return;
                    }
                }
                Log.i("MyHyphenate", "这是一条透传消息，不在当前页面");
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> list) {


            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> list) {

            }


            @Override
            public void onMessageChanged(EMMessage emMessage, Object o) {

            }
        };

    }

//    //listview的长按弹出
//    private void listViewItemPopupWindow() {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View contentView = inflater.inflate(R.layout.listview_item_listener_pop, null);
//        int h = this.getWindowManager().getDefaultDisplay().getHeight();
//        int w = this.getWindowManager().getDefaultDisplay().getWidth();
//        mPopWindow = new PopupWindow(contentView);
//        // 设置SelectPicPopupWindow的View
//        mPopWindow.setContentView(contentView);
//        // 设置SelectPicPopupWindow弹出窗体的宽
//        mPopWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
//        // 设置SelectPicPopupWindow弹出窗体的高
//        mPopWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
//        // 设置SelectPicPopupWindow弹出窗体可点击
//        mPopWindow.setFocusable(true);
//        mPopWindow.setOutsideTouchable(true);
//        // 刷新状态
//        mPopWindow.update();
//        // 实例化一个ColorDrawable颜色为半透明
//        ColorDrawable dw = new ColorDrawable(0000000000);
//        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
//        mPopWindow.setBackgroundDrawable(dw);
//        // 设置SelectPicPopupWindow弹出窗体动画效果
//        mPopWindow.setAnimationStyle(R.style.AnimationPreview);
//        LinearLayout addTaskLayout = (LinearLayout) contentView
//                .findViewById(R.id.clearAllContext);
//        LinearLayout teamMemberLayout = (LinearLayout) contentView
//                .findViewById(R.id.addBlock);
//        addTaskLayout.setOnClickListener(this);
//        teamMemberLayout.setOnClickListener(this);
//        showListViewPopupWindow(call);
//    }
//    public void showListViewPopupWindow(View parent) {
//        if (!mPopWindow.isShowing()) {
//            // 以下拉方式显示popupwindow
//            mPopWindow.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
//        } else {
//            mPopWindow.dismiss();
//        }
//    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //不需要的时候移除listener，如在activity的onDestroy()时
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }
}
