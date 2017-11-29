package com.longhuang.programme;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ProgrammeMenuActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_menu);
        Toolbar bar = findViewById(R.id.tool_bar_menu);

        setSupportActionBar(bar);
        initView();
        initEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){

        }
        return true;
    }
    private void initView(){

    }
    private void initEvent(){

    }
}
