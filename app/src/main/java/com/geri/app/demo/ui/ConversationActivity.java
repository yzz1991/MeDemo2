package com.geri.app.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.geri.app.demo.fragment.ConversationFragment;
import com.geri.app.ui.R;

/**
 * Created by Geri on 2016/10/26.
 */
public class ConversationActivity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame,new ConversationFragment()).commit();

    }

}
