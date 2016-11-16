package com.geri.app.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geri.app.ui.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


        private EditText ed_userName;
        private EditText ed_pwd;
        //弹出等待框
        private ProgressDialog mDialog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if(EMClient.getInstance().isLoggedInBefore()){
                startActivity(new Intent(this,MainActivity.class));
                finish();
            }

            setContentView(R.layout.activity_login);
            findView();
        }

        private void findView() {
            ed_userName = (EditText) findViewById(R.id.ed_userName);
            ed_pwd = (EditText) findViewById(R.id.ed_pwd);
            Button bt_login = (Button) findViewById(R.id.bt_login);
            Button tv_register = (Button) findViewById(R.id.bt_register);

            bt_login.setOnClickListener(this);
            tv_register.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.bt_login:
                    login();
                    break;

                case R.id.bt_register:
                    register();
                    break;
            }
        }

        //注册
        private void register() {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("注册中，请稍等...");
            mDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String username = ed_userName.getText().toString().trim();
                        String pwd = ed_pwd.getText().toString().trim();
                        EMClient.getInstance().createAccount(username, pwd);//同步方法
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!LoginActivity.this.isFinishing()){
                                    mDialog.dismiss();
                                }
                                Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (final HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!LoginActivity.this.isFinishing()){
                                    mDialog.dismiss();
                                }
                                Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                /**
                                 * 关于错误码可以参考官方api详细说明
                                 * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
                                 */
                                int errorCode = e.getErrorCode();
                                String message = e.getMessage();
                                Log.d("lzan13", String.format("sign up - errorCode:%d, errorMsg:%s", errorCode, e.getMessage()));
                                switch (errorCode) {
                                    // 网络错误
                                    case EMError.NETWORK_ERROR:
                                        Toast.makeText(LoginActivity.this, "网络错误 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                    // 用户已存在
                                    case EMError.USER_ALREADY_EXIST:
                                        Toast.makeText(LoginActivity.this, "用户已存在 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                    // 参数不合法，一般情况是username 使用了uuid导致，不能使用uuid注册
                                    case EMError.USER_ILLEGAL_ARGUMENT:
                                        Toast.makeText(LoginActivity.this, "参数不合法，一般情况是username 使用了uuid导致，不能使用uuid注册 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                    // 服务器未知错误
                                    case EMError.SERVER_UNKNOWN_ERROR:
                                        Toast.makeText(LoginActivity.this, "服务器未知错误 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                    //用户注册失败
                                    case EMError.USER_REG_FAILED:
                                        Toast.makeText(LoginActivity.this, "账户注册失败 code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        Toast.makeText(LoginActivity.this, "ml_sign_up_failed code: " + errorCode + ", message:" + message, Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                        });
                    }
                }
            }).start();
        }

        //登录
        private void login() {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("登录中，请稍等...");
            mDialog.show();
            String username = ed_userName.getText().toString().trim();
            String pwd = ed_pwd.getText().toString().trim();
            if(TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)){
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
                return;
            }
            //登录回调
            EMClient.getInstance().login(username,pwd,new EMCallBack() {
                @Override
                public void onSuccess() {
                    mDialog.dismiss();
                    //保证进入主页面后本地会话和群组都 load 完毕
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    //从服务器获取自己加入的和创建的群组列表，此api获取的群组sdk会自动保存到内存和db。
                    try {
                        List<EMGroup> grouplist = EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                        Log.d("LoginActivity", "grouplist:" + grouplist.size());
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                    //进入主页面
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                    Log.d("main", "登录聊天服务器成功！");
                }

                @Override
                public void onProgress(int progress, String status) {

                }

                @Override
                public void onError(int code, String message) {
                    Log.d("main", "登录聊天服务器失败！");
                }
            });
        }

}
