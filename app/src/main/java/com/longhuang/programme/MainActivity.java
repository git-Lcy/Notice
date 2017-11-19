package com.longhuang.programme;

import android.app.AlarmManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.L;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

public class MainActivity extends BaseActivity  {

    private RecyclerView recyclerView;
    private CheckBox switchInput;
    private EditText textInput;
    private Button voiceInput;
    private Button sendMessage;

    private ProgrammeAdapter adapter;

    private List<Programme> programmeList;
    private AlarmManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

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
            case R.id.skin:

                break;
            case R.id.add:
                if (adapter!=null){

                }
                break;
            case R.id.delete:
                if (adapter!=null){
                    adapter.deleteSelected();
                }
                break;
        }
        return true;
    }
    private void initView(){
        recyclerView = (RecyclerView) findViewById(R.id.programme_list);
        switchInput = (CheckBox)findViewById(R.id.switch_input_mode);
        textInput = (EditText)findViewById(R.id.text_input);
        voiceInput = (Button) findViewById(R.id.voice_input);
        sendMessage = (Button) findViewById(R.id.send_message);

    }

    private void initEvent(){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProgrammeAdapter(this);
        recyclerView.setAdapter(adapter );
        switchInput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    voiceInput.setVisibility(View.GONE);
                    textInput.setVisibility(View.VISIBLE);
                }else {
                    textInput.setVisibility(View.GONE);
                    voiceInput.setVisibility(View.VISIBLE);
                }
            }
        });

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //TODO nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                L.e(getClass(),"count = "+count);
                sendMessage.setEnabled(!(s.length()==0));
            }
            @Override
            public void afterTextChanged(Editable s) {
                //TODO nothing
            }
        });

        voiceInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textInput.getText().toString();
                textInput.setText("");
                Programme programme = new Programme();
                programme.setMessage(message);
                programme.save();
                adapter.addProgramme(programme);
            }
        });
    }

}
