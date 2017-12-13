package com.longhuang.programme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.longhuang.programme.module.Programme;

import org.litepal.crud.DataSupport;

public class ProgrammeNoticeShowActivity extends BaseActivity {

    private TextView noticeInfo;
    private ImageButton noticeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_setting);

        noticeInfo = findViewById(R.id.notice_info);
        noticeBtn = findViewById(R.id.notice_btn);

        String id = getIntent().getStringExtra("programmeId");
        if (TextUtils.isEmpty(id)) {
            finish();
            return;
        };
        Programme p = DataSupport.where("programmeId = ?",id).findFirst(Programme.class);
        if (p==null) {
            finish();
            return;
        }
        String message = p.getMessage();
        noticeInfo.setText(message);
        int repeat = p.getRepeatType();
        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
