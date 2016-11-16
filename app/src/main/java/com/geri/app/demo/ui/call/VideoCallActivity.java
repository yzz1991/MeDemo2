package com.geri.app.demo.ui.call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.geri.app.demo.MLCameraDataProcessor;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;

/**
 * Created by Geri on 2016/11/10.
 */
public class VideoCallActivity extends AppCompatActivity{

    private TextView mTestView;
    private EMCallManager.EMVideoCallHelper mVideoCallHelper;
    // 通话状态监听器
    private EMCallStateChangeListener callStateListener;
    // 显示对方画面控件
    private EMOppositeSurfaceView oppositeSurfaceView;
    // 显示自己方画面控件
    private EMLocalSurfaceView localSurfaceView;
    // 呼叫方名字
    private String username;
    // 是否是拨打进来的电话
    private boolean isInComingCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_video_call);
        username = getIntent().getStringExtra("username");
        isInComingCall = getIntent().getBooleanExtra("isComingCall",false);
        initView();
    }

    private void initView() {
        mTestView = (TextView) findViewById(R.id.ml_text_test);
        findViewById(R.id.ml_btn_answer_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_reject_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_end_call).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_mute).setOnClickListener(viewListener);
        findViewById(R.id.ml_btn_change_camera).setOnClickListener(viewListener);

        // 自己 surfaceview
        localSurfaceView = (EMLocalSurfaceView) findViewById(R.id.ml_surface_local);
        localSurfaceView.setZOrderMediaOverlay(true);
        localSurfaceView.setZOrderOnTop(true);

        // 对方 surfaceview
        oppositeSurfaceView = (EMOppositeSurfaceView) findViewById(R.id.ml_surface_opposite);

        // 设置视频通话双方显示画面控件
        EMClient.getInstance().callManager().setSurfaceView(localSurfaceView, oppositeSurfaceView);

        mVideoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();

        // 设置视频通话分辨率 默认是320，240
        mVideoCallHelper.setResolution(640, 480);
        // 设置比特率 默认是150
        mVideoCallHelper.setVideoBitrate(300);

        if(!isInComingCall){//fasle为拨打电话
            try {
                // 拨打视频电话
                EMClient.getInstance().callManager().makeVideoCall(username);
            } catch (EMServiceNotReadyException e) {
                e.printStackTrace();
            }
        }

        // 设置摄像头数据处理
        EMClient.getInstance().callManager().setCameraDataProcessor(new MLCameraDataProcessor());
        // 设置通话状态监听
        setCallStateListener();
    }

    //设置电话监听
    private void setCallStateListener() {

        callStateListener = new EMCallStateChangeListener() {

            @Override
            public void onCallStateChanged(CallState callState, final CallError error) {
                switch (callState) {
                    case CONNECTING: // 正在连接对方
                        Log.i("Voice", "正在连接对方");
                        break;
                    case CONNECTED: // 双方已经建立连接
                        Log.i("Voice", "双方已经建立连接");
                        break;
                    case ACCEPTED: // 电话接通成功
                        Log.i("Voice", "电话接通成功");
                        break;
                    case DISCONNECTED: // 电话断了
                        Log.i("Voice", "电话断了" + error);
                        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
                        finish();
                        break;
                    case NETWORK_UNSTABLE:
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (error == CallError.ERROR_NO_DATA) {
                                    Log.i("Voice", "没有通话数据" + error);
                                } else {
                                    Log.i("Voice", "网络不稳定" + error);
                                }
                            }
                        });
                        break;
                    case NETWORK_NORMAL:
                        Log.i("Voice", "网络正常");
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Voice", "接听失败");
                        finish();
                        return;
                    }
                    break;
                case R.id.ml_btn_reject_call:
                    // 拒绝接听对方的呼叫
                    try {
                        EMClient.getInstance().callManager().rejectCall();
                        Log.i("Voice", "拒绝接听呼叫");
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Voice", "拒绝接听失败");
                        finish();
                    }
                    break;
                case R.id.ml_btn_end_call:
                    // 挂断通话
                    try {
                        EMClient.getInstance().callManager().endCall();
                        Log.i("Voice", "挂断电话");
                    } catch (EMNoActiveCallException e) {
                        Log.i("Voice", "挂断电话失败");
                        finish();
                        e.printStackTrace();
                    }
                    break;
                default:
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
