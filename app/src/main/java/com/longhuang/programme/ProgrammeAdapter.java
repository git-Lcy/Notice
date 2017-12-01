package com.longhuang.programme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.ProgrammeComparator;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/11/17.
 */

public class ProgrammeAdapter extends RecyclerView.Adapter {

    private List<Programme> programmeList ;
    private List<Programme> programmeSelectedDate ;
    private LayoutInflater inflater;
    private Context context;
    private boolean isCalendarData;

    public ProgrammeAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
        programmeList = Global.programmeList;
    }

    public void setProgrammeSelectedDate(List<Programme> programmes){
        if (programmeSelectedDate==null) programmeSelectedDate = new ArrayList<>();
        if (programmeSelectedDate.size()>0) programmeSelectedDate.clear();
        programmeSelectedDate.addAll(programmes);
        isCalendarData = true;
    }
    public void showAllProgramme(){
        if (!isCalendarData) return;
        isCalendarData = false;
        notifyDataSetChanged();
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
        final Programme programme = isCalendarData ? programmeSelectedDate.get(position) : programmeList.get(position);
        final ProgrammeHolder programmeHolder = (ProgrammeHolder)holder;
        programmeHolder.messageInfo.setText(programme.getMessage());
        String time = programme.getTimeInfo();
        if (TextUtils.isEmpty(time)){
            programmeHolder.timeInfo.setVisibility(View.GONE);
        }else {
            programmeHolder.timeInfo.setVisibility(View.VISIBLE);
            programmeHolder.timeInfo.setText(time);
        }

        programmeHolder.ringing.setChecked(programme.isRinging());
        programmeHolder.vibrate.setChecked(programme.isVibrate());

        programmeHolder.layout.setBackgroundColor(
                    context.getResources().getColor(programme.isSelected ? R.color.colorItemBg : R.color.transparent));

        programmeHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.programmeCache==null)  Global.programmeCache = new ArrayList<>();
                boolean selected = programme.isSelected;
                if (selected){
                    Global.programmeCache.remove(programme);
                }else {
                    Global.programmeCache.add(programme);
                }
                programme.isSelected = !selected;
                notifyItemChanged(position);

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
        if (isCalendarData)  return programmeSelectedDate.size();

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

    public void insertProgramme(Programme programme){
        if (isCalendarData)programmeSelectedDate.add(0, programme);
        programmeList.add(0,programme);
        notifyItemInserted(0);
    }
    public void deleteSelected(){
        for (Programme programme : Global.programmeCache){
            programme.isSelected = false;
            programme.delete();
            programmeList.remove(programme);
            if (isCalendarData) programmeSelectedDate.remove(programme);
        }
        notifyDataSetChanged();
    }

    public void add(Programme programme){
        if (Global.programmeCache.size()==0){
            programmeList.add(0,programme);
            notifyItemInserted(0);
            return;
        }
        Programme editProgramme = Global.programmeCache.get(0);
        editProgramme.setDescription(programme.getDescription());
        editProgramme.setMessage(programme.getMessage());
        editProgramme.setRinging(programme.isRinging());
        editProgramme.setVibrate(programme.isVibrate());
        editProgramme.setRingingUrl(programme.getRingingUrl());
        editProgramme.setTime(programme.getTime());
    }
}
