package com.geri.app.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geri.app.demo.adapter.BlockListAdapter;
import com.geri.app.ui.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by Geri on 2016/11/4.
 */
public class BlockListActivity extends AppCompatActivity{

    private String groupId;
    private ListView blockLv;
    private List<String> blockedUsers;
    private BlockListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocklist);

        groupId = getIntent().getStringExtra("groupId");
        initView();
    }

    private void initView() {
        TextView blockBack = (TextView) findViewById(R.id.block_back);
        blockLv = (ListView) findViewById(R.id.blockLv);

        getBlackUser();

        blockBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        this.registerForContextMenu(blockLv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, Menu.NONE, "移出黑名单");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case 0:
                for(int i =0; i<blockedUsers.size(); i++){
                    if(blockedUsers.get(menuInfo.position).equals(blockedUsers.get(i))){
                        removeblackUser(blockedUsers.get(menuInfo.position));
                        adapter.notifyDataSetChanged();
                        finish();
                    }else{
                        Toast.makeText(this, "不在黑名单列表", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    //获取黑名单
    public void getBlackUser(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    blockedUsers =  EMClient.getInstance().groupManager().getBlockedUsers(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new BlockListAdapter(BlockListActivity.this, blockedUsers);
                            blockLv.setAdapter(adapter);
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //将群成员移出群组的黑名单
    public void removeblackUser(final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().unblockUser(groupId, username);

                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
