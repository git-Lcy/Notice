package com.longhuang.programme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.cloud.SpeechSynthesizer;
import com.longhuang.programme.imp.AlarmBroadcastReceiver;
import com.longhuang.programme.imp.MySynthesizerListener;
import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/17.
 */

public class ProgrammeAdapter extends RecyclerView.Adapter {

    private List<ExtraProgramme> programmeList ;
    private List<ExtraProgramme> programmeSelectedDate ;
    private LayoutInflater inflater;
    private Context context;
    private boolean isCalendarData;

    public ProgrammeAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
        programmeList = Global.programmeList;
    }

    public void setProgrammeSelectedDate(List<ExtraProgramme> programmes){
        if (programmeSelectedDate==null) programmeSelectedDate = new ArrayList<>();
        if (programmeSelectedDate.size()>0) programmeSelectedDate.clear();
        programmeSelectedDate.addAll(programmes);
        isCalendarData = true;
    }
    public void showAllProgramme(){
        if (!isCalendarData) return;
        isCalendarData = false;
        Global.clearCache();
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
        final ExtraProgramme extra = isCalendarData ? programmeSelectedDate.get(position) : programmeList.get(position);
        final Programme programme = extra.getProgramme();
        final ProgrammeHolder programmeHolder = (ProgrammeHolder)holder;

        programmeHolder.messageInfo.setText(programme.getMessage());

        String time = programme.getTime();
        String date = programme.getDate();

        if (TextUtils.isEmpty(date)){
            programmeHolder.timeInfo.setVisibility(View.GONE);
        }else {
            if (time==null) {
                time = "";
            }else {
                time = " "+time;
            }

            programmeHolder.timeInfo.setVisibility(View.VISIBLE);
            programmeHolder.timeInfo.setText(date+time);
        }

        programmeHolder.ringing.setChecked(programme.isRinging());
        programmeHolder.vibrate.setChecked(programme.isVibrate());

        programmeHolder.layout.setBackgroundColor(context.getResources()
                        .getColor(extra.isSelected ? R.color.colorItemBg : R.color.transparent));

        programmeHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = extra.isSelected;
                if (selected){
                    Global.programmeCache.remove(extra);
                }else {
                    Global.programmeCache.add(extra);
                }
                extra.isSelected = !selected ;
                notifyItemChanged(isCalendarData ? programmeSelectedDate.indexOf(extra) : programmeList.indexOf(extra));
            }
        });
        programmeHolder.ringing.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               if (event.getAction() == MotionEvent.ACTION_UP) {

                   boolean isRinging = !programmeHolder.ringing.isChecked();
                   programmeHolder.ringing.setChecked(isRinging);
                   programme.setRinging(isRinging);
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

        programmeHolder.voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpeechSynthesizer.getSynthesizer() == null) return;
                SpeechSynthesizer sp = SpeechSynthesizer.getSynthesizer();
                boolean voice = programmeHolder.voice.isChecked();
                if (voice){
                    sp.startSpeaking(programme.getMessage(),new MySynthesizerListener(new MySynthesizerListener.OnSpeakCompleted() {
                        @Override
                        public void onCompleted() {
                            programmeHolder.voice.setChecked(false);
                        }
                    }));
                }else if (sp.isSpeaking()){
                    sp.stopSpeaking();
                }

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
        private TextView timeInfo;//提醒执行时间
        private TextView messageInfo;// 提醒信息
        private CheckBox vibrate;//震动
        private CheckBox ringing;//铃声
        private CheckBox voice;//语音

        public ProgrammeHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.programme_item);
            timeInfo = itemView.findViewById(R.id.programme_time);
            messageInfo = itemView.findViewById(R.id.programme_message);
            vibrate = itemView.findViewById(R.id.programme_switch_vibrate);
            ringing = itemView.findViewById(R.id.programme_switch_ringing);
            voice = itemView.findViewById(R.id.programme_play_voice);
        }
    }

    //添加提醒并刷新
    public void insertProgramme(ExtraProgramme programme){
        if (isCalendarData)programmeSelectedDate.add(0, programme);
        programmeList.add(0,programme);
        notifyItemInserted(0);
    }
    // 删除选中的提醒
    public void deleteSelected(){
        if (Global.programmeCache.size()==0) return;
        for (ExtraProgramme programme : Global.programmeCache){
            Programme p = programme.getProgramme();
            p.delete();
            if (isCalendarData) {
                programmeSelectedDate.remove(programme);
                int size = programmeList.size()-1;
                for (int i = size;i>=0;i--){
                    if (TextUtils.equals(programmeList.get(i).getProgramme().getProgrammeId(),programme.getProgramme().getProgrammeId())){
                        programmeList.remove(i);
                    }
                }
            }else {
                programmeList.remove(programme);
            }
        }
        notifyDataSetChanged();
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context,AlarmBroadcastReceiver.class);
                for (ExtraProgramme programme : Global.programmeCache){
                    Programme p = programme.getProgramme();
                    int request = programme.getProgramme().getRequestCode();
                    intent.putExtra("programmeId",p.getProgrammeId());
                    intent.putExtra("executeTime",p.getExecuteTime());
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(context,request,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.cancel(pendingIntent);
                }
                Global.clearCache();
            }
        });
    }

}
