package com.geri.app.demo.ui.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.geri.app.demo.MLConstants;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;

/**
 * Created by Geri on 2016/11/10.
 */
public class VoiceCallActivity extends CallActivity{

    // 呼叫方名字
    private String username;
    // 是否是拨打进来的电话
    private boolean isInComingCall;
    // 通话状态监听器
    private EMCallStateChangeListener callStateListener;
    private TextView mCallStatusView;
    private Chronometer chronometerCallTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        // 注册语音电话的状态的监听
        addCallStateListener();

        username = getIntent().getStringExtra("username");
        isInComingCall = getIntent().getBooleanExtra("isComingCall",false);

        initView();

    }

    private void initView() {
        mCallStatusView = (TextView) findViewById(R.id.text_call_status);
        chronometerCallTime = (Chronometer) findViewById(R.id.chronometer_call_time);
        findViewById(R.id.ml_btn_answer_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_reject_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_end_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_mute).setOnClickListener(viewListener);

        if(!isInComingCall){//fasle为拨打电话
            try {
                // 拨打语音电话
                EMClient.getInstance().callManager().makeVoiceCall(username);
            } catch (EMServiceNotReadyException e) {
                e.printStackTrace();
            }
        }
    }

    //设置电话监听
    private void addCallStateListener() {

            callStateListener = new EMCallStateChangeListener() {

                @Override
                public void onCallStateChanged(CallState callState, final CallError error) {

                    switch (callState) {
                        case CONNECTING: // 正在连接对方
//                            Log.i("Voice", "正在连接对方");
                            mCallStatusView.setText("正在连接对方");
                            break;
                        case CONNECTED: // 双方已经建立连接
//                            Log.i("Voice", "双方已经建立连接");
                            mCallStatusView.setText("正在等待对方接受呼叫申请");
                            break;
                        case ACCEPTED: // 电话接通成功
//                            Log.i("Voice", "电话接通成功");
//                            mCallStatusView .setText("电话已接通");
                            // 通话已接通，设置通话状态为正常状态
                            mCallStatus = MLConstants.ML_CALL_ACCEPTED;
                            // 开始计时
//                            mChronometer.setBase(SystemClock.elapsedRealtime());
//                            mChronometer.start();

                            break;
                        case DISCONNECTED: // 电话断了
//                            Log.i("Voice", "电话断了" + error);
                            // 停止计时
//                            mChronometer.stop();
                            mCallStatusView.setText("通话已结束");
                            if(error == CallError.ERROR_UNAVAILABLE){
                                // 设置通话状态为对方不在线
                                mCallStatus = MLConstants.ML_CALL_OFFLINE;
                                mCallStatusView.setText("对方不在线");
                            }else if(error == CallError.REJECTED){
                                // 设置通话状态为对方已拒绝
                                mCallStatus = MLConstants.ML_CALL_REJECT;
                                mCallStatusView.setText("对方已拒绝");
                            }else if(error == CallError.ERROR_NORESPONSE){
                                // 设置通话状态为对方未响应
                                mCallStatus = MLConstants.ML_CALL_NORESPONSE;
                                mCallStatusView.setText("对方手机不在身边，稍后再试");
                            }else if(error == CallError.ERROR_TRANSPORT){
                                // 设置通话状态为建立连接失败
                                mCallStatus = MLConstants.ML_CALL_TRANSPORT;
                                mCallStatusView.setText("建立连接失败");
                            }else if(error == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED){
                                // 设置通话状态为双方协议不同
                                mCallStatus = MLConstants.ML_CALL_VERSION_DIFFERENT;
                                mCallStatusView.setText("本地版本过低，无法通讯");
                            }else if(error == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED){
                                // 设置通话状态为双方协议不同
                                mCallStatus = MLConstants.ML_CALL_VERSION_DIFFERENT;
                                mCallStatusView.setText("对方版本过低，无法通讯");
                            }else {
                                // 根据当前状态判断是正常结束，还是对方取消通话
                                if (mCallStatus == MLConstants.ML_CALL_CANCEL) {
                                    // 设置通话状态
                                    mCallStatus = MLConstants.ML_CALL_CANCEL_INCOMING_CALL;
                                }
                                mCallStatusView.setText("对方已取消");
                            }
                            // 通话结束保存消息
                            saveCallMessage();
                            // 结束通话时取消通话状态监听
                            EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
                            // 结束通话关闭界面
                            finish();
                            break;
                        case NETWORK_UNSTABLE:
                            if (error == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                                Log.i("VoiceCallActivity", "没有通话数据" + error);
                                mCallStatusView.setText("没有通话数据");
                            } else {
                                Log.i("VoiceCallActivity","网络不稳定" + error);
                                mCallStatusView.setText("网络不稳定");
                            }
                            break;
                        case NETWORK_NORMAL:
                            Log.i("Voice", "网络正常");
                            mCallStatusView.setText("网络状态良好");
                            break;
                        default:
                            break;
                    }

                }
            };
            EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
    }

    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ml_btn_answer_call:
                    // 接听对方的呼叫
                    try {
                        Log.i("Voice", "正在接听...");
                        EMClient.getInstance().callManager().answerCall();
                        Log.i("Voice", "接听成功");
                        // 设置通话状态为正常结束
                        mCallStatus = MLConstants.ML_CALL_ACCEPTED;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Voice", "接听失败");
                        finish();
                        return;
                    }
                    break;
                case R.id.ml_btn_reject_call:
                    EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
                    // 拒绝接听对方的呼叫
                    try {
                        EMClient.getInstance().callManager().rejectCall();
                        Log.i("Voice", "拒绝接听呼叫");
                        // 保存一条通话消息
                        saveCallMessage();
                        // 结束界面
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Voice", "拒绝接听失败");
                        finish();
                    }
                    break;
                case R.id.ml_btn_end_call:
                    EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
                    // 挂断通话
                    try {
                        EMClient.getInstance().callManager().endCall();
                        Log.i("Voice", "挂断电话");
                        // 挂断电话调用保存消息方法
                        saveCallMessage();
                        // 结束界面
                        finish();
                    } catch (EMNoActiveCallException e) {
                        Log.i("Voice", "挂断电话失败");
                        finish();
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        try {
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
        finish();
    }
}
