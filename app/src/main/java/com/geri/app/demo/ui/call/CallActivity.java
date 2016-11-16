package com.geri.app.demo.ui.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.geri.app.demo.MLConstants;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

/**
 * Created by Geri on 2016/11/14.
 */

public class CallActivity extends AppCompatActivity{

    // 呼叫方名字
    protected String mChatId;
    // 是否是拨打进来的电话
    protected boolean isInComingCall;
    // 通话结束状态，用来保存通话结束后的消息提示
    protected int mCallStatus;
    // 通话计时控件
    protected Chronometer mChronometer;
    // 通话类型，用于区分语音和视频通话 0 代表视频，1 代表语音
    protected int mCallType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置通话界面属性，保持屏幕常亮，关闭输入法，以及解锁
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * 通话结束，保存一条记录通话的消息
     */
    protected void saveCallMessage() {
        EMMessage message = null;
        EMTextMessageBody body = null;
        String content = null;
        if (isInComingCall) {
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            message.setFrom(mChatId);
        } else {
            message = EMMessage.createSendMessage(EMMessage.Type.TXT);
            message.setReceipt(mChatId);
        }

        switch (mCallStatus) {
            case MLConstants.ML_CALL_ACCEPTED:
                // 通话正常结束，要加上通话时间
                content = "通话正常挂断";
                break;
            case MLConstants.ML_CALL_CANCEL:
                // 自己取消
                content = "已取消";
                break;
            case MLConstants.ML_CALL_CANCEL_INCOMING_CALL:
                // 对方取消
                content = "对方已取消";
                break;
            case MLConstants.ML_CALL_BUSY:
                // 对方正忙
                content = "对方正忙";
                break;
            case MLConstants.ML_CALL_OFFLINE:
                // 对方不在线
                content = "对方不在线";
                break;
            case MLConstants.ML_CALL_REJECT_INCOMING_CALL:
                // 自己已拒绝
                content = "已拒绝";
                break;
            case MLConstants.ML_CALL_REJECT:
                // 对方拒绝
                content = "对方已拒绝";
                break;
            case MLConstants.ML_CALL_NORESPONSE:
                // 对方无响应
                content = "对方无响应";
                break;
            case MLConstants.ML_CALL_TRANSPORT:
                // 建立连接失败
                content = "对方手机可能不在身边，请稍后再试";
                break;
            case MLConstants.ML_CALL_VERSION_DIFFERENT:
                // 双方通话协议版本不同
                content = "对方不在线";
                break;
            default:
                // 默认为取消
                content = "已取消";
                break;
        }
        body = new EMTextMessageBody(content);
        message.addBody(body);
        message.setStatus(EMMessage.Status.SUCCESS);
        message.setMsgId(System.currentTimeMillis() + "");
        if (mCallType == 0) {
            message.setAttribute(MLConstants.ML_ATTR_CALL_VIDEO, true);
        } else {
            message.setAttribute(MLConstants.ML_ATTR_CALL_VOICE, true);
        }
        // 调用sdk的保存消息方法
        EMClient.getInstance().chatManager().saveMessage(message);
    }
}
