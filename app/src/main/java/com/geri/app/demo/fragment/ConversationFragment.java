package com.geri.app.demo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.geri.app.ui.R;

/**
 * Created by Geri on 2016/11/16.
 */

public class ConversationFragment extends Fragment{

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation,null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
    }

    private void initView() {
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setText("会话");
        ListView conversationView = (ListView) view.findViewById(R.id.contactsView);

    }
}
