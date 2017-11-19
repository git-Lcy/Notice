package com.longhuang.programme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.longhuang.programme.module.Programme;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/11/17.
 */

public class ProgrammeAdapter extends RecyclerView.Adapter {

    private List<Programme> programmeCache ;
    private List<Programme> programmeList ;
    private LayoutInflater inflater;
    private Context context;

    public ProgrammeAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
        Connector.getDatabase();
        programmeList = DataSupport.findAll(Programme.class);
        if (programmeList==null) programmeList = new ArrayList<>();
        programmeCache = new ArrayList<>();
    }
    public void addProgramme(Programme programme){
        programmeList.add(programme);
        notifyItemInserted(programmeList.size()-1);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.programme_item,parent,false);
        ProgrammeHolder holder = new ProgrammeHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Programme programme = programmeList.get(position);
        final ProgrammeHolder programmeHolder = (ProgrammeHolder)holder;
        programmeHolder.messageInfo.setText(programme.getMessage());
        programmeHolder.timeInfo.setText(programme.getTime());
        programmeHolder.ringing.setChecked(programme.isRinging());
        programmeHolder.vibrate.setChecked(programme.isVibrate());
        programmeHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (programmeCache==null) programmeCache = new ArrayList<>();
                boolean selected = programmeHolder.layout.isSelected();
                if (selected){
                    programmeCache.remove(programme);
                }else {
                    programmeCache.add(programme);
                }
                programmeHolder.layout.setSelected(!selected);
            }
        });
        programmeHolder.ringing.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               if (event.getAction() == MotionEvent.ACTION_UP) {
                   boolean isRinging = !programmeHolder.ringing.isChecked();
                   programmeHolder.ringing.setChecked(isRinging);
                   programme.setVibrate(isRinging);
                   programme.save();
                   return true;
               }
               return false;
           }
       });
        programmeHolder.vibrate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    boolean isVibrate = !programmeHolder.vibrate.isChecked();
                    programmeHolder.vibrate.setChecked(isVibrate);
                    programme.setVibrate(isVibrate);
                    programme.save();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return programmeList.size();
    }

    class ProgrammeHolder extends RecyclerView.ViewHolder{
        private LinearLayout layout;
        private TextView timeInfo;
        private TextView messageInfo;
        private CheckBox vibrate;//震动
        private CheckBox ringing;//铃声

        public ProgrammeHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.programme_item);
            timeInfo = itemView.findViewById(R.id.programme_time);
            messageInfo = itemView.findViewById(R.id.programme_message);
            vibrate = itemView.findViewById(R.id.programme_switch_vibrate);
            ringing = itemView.findViewById(R.id.programme_switch_ringing);

        }
    }

    public void deleteSelected(){
        for (Programme programme : programmeCache){
            programmeList.remove(programme);
        }
        notifyDataSetChanged();
    }
    public int editType(){
        int size = programmeCache.size();
        if (size == 0) return 0;
        if (size == 1) return 1;
        return -1;
    }
    public void add(Programme programme){
        if (programmeCache.size()==0){
            programmeList.add(0,programme);
            notifyItemInserted(0);
            return;
        }
        Programme editProgramme = programmeCache.get(0);
        editProgramme.setDescription(programme.getDescription());
        editProgramme.setMessage(programme.getMessage());
        editProgramme.setRinging(programme.isRinging());
        editProgramme.setVibrate(programme.isVibrate());
        editProgramme.setRingingUrl(programme.getRingingUrl());
        editProgramme.setTime(programme.getTime());
    }
}
