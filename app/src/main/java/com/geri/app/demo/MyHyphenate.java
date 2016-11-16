package com.geri.app.demo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Geri on 2016/10/25.
 */

public class MyHyphenate {
    // 上下文对象
    private Context mContext;
    // MLHyphenate 单例对象
    private static MyHyphenate instance;
    // 记录sdk是否初始化
    private boolean isInit;
    private EMConnectionListener mConnectionListener;
    private EMMessageListener mMessageListener;
    // 保存当前运行的 activity 对象，可用来判断程序是否处于前台，以及完全退出app等操作
    private List<AppCompatActivity> mActivityList = new ArrayList<AppCompatActivity>();
    private EMContactListener mContactListener;
    private EMGroupChangeListener mGroupChangeListener;
    private CallReceiver callReceiver;

    public static MyHyphenate getInstance() {
        if (instance == null) {
            instance = new MyHyphenate();
        }
        return instance;
    }

    private MyHyphenate() {
    }

    public synchronized boolean initData(Context context) {
        mContext = context;
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果APP启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的APP会在以包名为默认的process name下运行，如果查到的process name不是APP的process name就立即返回

        if (processAppName == null ||!processAppName.equalsIgnoreCase(context.getPackageName())) {
            Log.e("application", "enter the service process!");
            // 则此application::onCreate 是被service 调用的，直接返回
            return true;
        }
        if(isInit){
            return isInit;
        }
        mContext = context;
        //初始化
        EMClient.getInstance().init(mContext, initOptions());
        //注册全局监听
        initGlobalListener();
        //初使化完成
        isInit = true;
        return isInit;
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = mContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return null;
    }

    private EMOptions initOptions(){
        EMOptions options = new EMOptions();
        // 设置自动登录
        options.setAutoLogin(true);
        // 设置是否需要发送已读回执
        options.setRequireAck(true);
        // 设置是否需要发送回执，
        options.setRequireDeliveryAck(true);
        // 设置是否需要服务器收到消息确认
        options.setRequireServerAck(true);
        // 设置是否根据服务器时间排序，默认是true
        options.setSortMessageByServerTime(false);
        // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
        options.setAcceptInvitationAlways(false);
        // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
        options.setAutoAcceptGroupInvitation(false);
        // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
        options.setDeleteMessagesAsExitGroup(true);
        // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
        options.allowChatroomOwnerLeave(true);
        //设置小米推送key
        options.setMipushConfig("2882303761517520771", "5171752014771");
        return options;

    }
    //注册全局监听
    private void initGlobalListener() {
        //网络监听
        setConnectionListener();
        //全局消息监听
        setMessageListener();
        //全局联系人监听
        setContactListener();
        // 设置全局的群组变化监听
        setGroupChangeListener();
        //设置通话广播监听
        setCallIntentFilter();
    }

    private void setCallIntentFilter() {
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }

        //注册通话广播接收者
        mContext.registerReceiver(callReceiver, callFilter);
    }

    private void setConnectionListener() {
        mConnectionListener = new EMConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(int errorCode) {
                if(errorCode == EMError.USER_REMOVED){
                    Log.i("MyHyphenate", "显示帐号已经被移除");
                    signOut(null);
                }else if (errorCode == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    Log.i("MyHyphenate", "显示帐号在其他设备登录");
                    signOut(null);
                } else {
                    Log.i("MyHyphenate", "当前网络不可用，请检查网络设置");
                }
            }
        };
        EMClient.getInstance().addConnectionListener(mConnectionListener);
    }
    //消息监听
    private void setMessageListener() {
        mMessageListener = new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> list) {
                if(MyHyphenate.getInstance().getActivityList().size()>0){
                    if(MyHyphenate.getInstance().getTopActivity().getClass().getSimpleName().equals("ChatActivity")){
                        return;
                    }
                }
                // 遍历消息集合
                for (EMMessage message : list) {
                    // 使用 EventBus 发布消息，可以被订阅此类型消息的订阅者监听到
                    MLMessageEvent event = new MLMessageEvent();
                    event.setMessage(message);
                    event.setStatus(message.status());
                    EventBus.getDefault().post(event);
                }
                if (list.size() > 1) {
                    // 收到多条新消息，发送一条消息集合的通知
                    MLNotifier.getInstance().sendNotificationMessageList(list);
                } else {
                    // 只有一条消息，发送单条消息的通知
                    MLNotifier.getInstance().sendNotificationMessage(list.get(0));
                }
                Log.i("message","消息不是当前页面的");
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
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    private void setContactListener() {
        mContactListener = new EMContactListener() {
            @Override
            public void onContactAdded(String s) {
//                Toast.makeText(mContext, "新增了联系人", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactDeleted(String s) {
//                Toast.makeText(mContext, "您被好友删除了", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactInvited(String s, String s1) {
//                Toast.makeText(mContext, "收到好友请求", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactAgreed(String s) {
//                Toast.makeText(mContext, "对方已经同意了您的好友请求", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactRefused(String s) {
//                Toast.makeText(mContext, "对方拒绝了您的好友请求", Toast.LENGTH_SHORT).show();
            }
        };
        EMClient.getInstance().contactManager().setContactListener(mContactListener);
    }
    //群组监听
    private void setGroupChangeListener() {
        mGroupChangeListener = new EMGroupChangeListener() {
            //收到加入群组的邀请
            @Override
            public void onInvitationReceived(String s, String s1, String s2, String s3) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2+"邀请加入"+s1);
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("groupName",s1);
                message.setAttribute("inviter",s2);
                message.setAttribute("reason",s3);
                message.setAttribute("status","");
                message.setAttribute("isfrom","invite");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            //收到加群申请
            @Override
            public void onApplicationReceived(String s, String s1, String s2, String s3) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2+"申请加入"+s1);
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("groupName",s1);
                message.setAttribute("inviter",s2);
                message.setAttribute("reason",s3);
                message.setAttribute("status","");
                message.setAttribute("isfrom","apply");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            //加群申请被同意
            @Override
            public void onApplicationAccept(String s, String s1, String s2) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2+"申请加"+s1+"被同意");
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("groupName",s1);
                message.setAttribute("inviter",s2);
                message.setAttribute("status","被同意");
                message.setAttribute("isfrom","WasAgreed");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);

            }
            // 加群申请被拒绝
            @Override
            public void onApplicationDeclined(String s, String s1, String s2, String s3) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2+"申请加"+s1+"被拒绝");
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("groupName",s1);
                message.setAttribute("inviter",s2);
                message.setAttribute("reason",s3);
                message.setAttribute("status","被拒绝");
                message.setAttribute("isfrom","WasFefuse");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            //群组邀请被接受
            @Override
            public void onInvitationAccepted(String s, String s1, String s2) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody("群组邀请"+s1+"被接受");
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("inviter",s1);
                message.setAttribute("reason",s2);
                message.setAttribute("status","被接受");
                message.setAttribute("isfrom","beijieshou");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            //群组邀请被拒绝
            @Override
            public void onInvitationDeclined(String s, String s1, String s2) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody("群组邀请"+s1+"被拒绝");
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("inviter",s1);
                message.setAttribute("reason",s2);
                message.setAttribute("status","被拒绝");
                message.setAttribute("isfrom","beijujue");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }

            //当前用户被管理员移除出群组
            @Override
            public void onUserRemoved(String s, String s1) {
                String msgId = String.valueOf(System.currentTimeMillis());
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody("您已被群主移出群");
                message.addBody(body);
                message.setAttribute("groupId",s);
                message.setAttribute("groupName",s1);
                message.setAttribute("status","被移出");
                message.setAttribute("isfrom","beiyichu");
                message.setTo("通知");
                message.setMsgId(msgId);
                EMClient.getInstance().chatManager().saveMessage(message);
            }

            @Override
            public void onGroupDestroyed(String s, String s1) {
                //群组被解散
            }

            /**
             * 自动同意加入群组 sdk会先加入这个群组，并通过此回调通知应用
             *
             * @param s 收到邀请加入的群组id
             * @param s1 邀请者
             * @param s2 邀请信息
             */
            @Override
            public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
                //
            }
        };
        EMClient.getInstance().groupManager().addGroupChangeListener(mGroupChangeListener);
    }

    //退出登录
    public void signOut(final EMCallBack callBack){
        EMClient.getInstance().logout(true, new EMCallBack() {
            @Override
            public void onSuccess() {
                if(callBack != null){
                    callBack.onSuccess();
                }
            }

            @Override
            public void onError(int i, String s) {
                if(callBack != null){
                    callBack.onError(i,s);
                }
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }


    /**
     * 获取当前运行启动的 activity 的列表
     *
     * @return 返回保存列表
     */
    public List<AppCompatActivity> getActivityList() {
        return mActivityList;
    }


    /**
     * 获取当前运行的 activity
     *
     * @return 返回当前活动的activity
     */
    public AppCompatActivity getTopActivity() {
        if (mActivityList.size() > 0) {
            return mActivityList.get(0);
        }
        return null;
    }

    /**
     * 添加当前activity到集合
     *
     * @param activity 需要添加的 activity
     */
    public void addActivity(AppCompatActivity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(0, activity);
        }
    }

    /**
     * 从 Activity 运行列表移除当前要退出的 activity
     *
     * @param activity 要移除的 activity
     */
    public void removeActivity(AppCompatActivity activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);
        }
    }

}
