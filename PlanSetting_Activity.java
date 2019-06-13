package com.liuzozo.stepdemo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 运动计划设置页面
 * 弹框  设置每天跑步目标公里， 是否提醒， 提醒时间
 */

public class PlanSetting_Activity extends Activity implements View.OnClickListener {

    private static Context sContext = null;
    private Calendar calendar = Calendar.getInstance();
    private int mHour;
    private Integer mMinute;

    private Switch btn;
    private TextView targetstep, alarmTime;
    private LinearLayout ll_target;
    private SharedPreferences myPlan;
    private SharedPreferences.Editor editor;
    private Calendar mCalendar;


    public static Context getContext() {
        return sContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paln_setting);
        sContext = this;
        initView();
        init();
    }

    private void initView() {

        btn = (Switch) findViewById(R.id.switch3);
        alarmTime = (TextView) findViewById(R.id.alarmTime);
        targetstep = (TextView) findViewById(R.id.targetstep);
        ll_target = (LinearLayout) findViewById(R.id.ll_target);

        myPlan = this.getSharedPreferences("myPlan", Context.MODE_PRIVATE);
        if (myPlan.getInt("target", 0) != 0) {
            targetstep.setText(myPlan.getInt("target", 0) + "");
        }
        if (myPlan.getInt("isSet", 0) == 0) {
            btn.setChecked(false);
        } else
            btn.setChecked(true);

        if (!myPlan.getString("alarmTime", "").equals(""))
            alarmTime.setText(myPlan.getString("alarmTime", ""));
    }

    private void init() {
        btn.setOnClickListener(this);
        ll_target.setOnClickListener(this);
        alarmTime.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        myPlan = this.getSharedPreferences("myPlan", Context.MODE_PRIVATE);
        editor = myPlan.edit();

        Log.i("tt", "" + v.getId());
        switch (v.getId()) {

            case R.id.alarmTime:
                showTimeDialog(alarmTime);
                break;

            case R.id.switch3:
                if (btn.isChecked()) {
                    if (!alarmTime.getText().toString().equals("")) {
                        Log.i("switch", "on");
                        editor.putInt("isSet", 1);
                        editor.apply();
                        startRemind();
                    } else {
                        Toast.makeText(getContext(), "请先选择提醒时间", Toast.LENGTH_SHORT).show();
                        btn.setChecked(false);
                        Log.i("switch", "off");
                    }

                } else {
                    stopRemind();
                    editor.putInt("isSet", 0);
                    editor.apply();
                    Log.i("switch", "off");
                }

                break;

            case R.id.ll_target:

                showTargetDialog(v);

                break;

            default:

                break;
        }
    }

    public void showTimeDialog(final TextView time) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = (LinearLayout) View.inflate(this,
                R.layout.time_dialog, null);
        myPlan = this.getSharedPreferences("myPlan", Context.MODE_PRIVATE);
        editor = myPlan.edit();

        final TimePicker timePicker = (TimePicker) view
                .findViewById(R.id.time_picker);
        // 初始化时间
        calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(Calendar.MINUTE);
        // 设置time布局
        builder.setView(view);
        builder.setTitle("设置提醒时间");
        builder.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        mHour = timePicker.getCurrentHour();
                        mMinute = timePicker.getCurrentMinute();
                        // 时间小于10的数字 前面补0 如01:12:00
                        time.setText(new StringBuilder()
                                .append(mHour < 10 ? "0" + mHour
                                        : mHour)
                                .append(":")
                                .append(mMinute < 10 ? "0" + mMinute
                                        : mMinute));
                        editor.putString("alarmTime", alarmTime.getText().toString());
                        editor.apply();
                        if (btn.isChecked())
                            startRemind();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("取  消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();

    }

    public void showTargetDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置步数");

        myPlan = this.getSharedPreferences("myPlan", Context.MODE_PRIVATE);
        editor = myPlan.edit();

        final View v = getLayoutInflater().inflate(R.layout.step_setting_popup_window, null);
        builder.setView(v);

        final EditText step_setting = (EditText) v.findViewById(R.id.step_setting);
        if (myPlan.getInt("target", 0) != 0) {
            step_setting.setHint(myPlan.getInt("target", 0) + "");
        }

        builder.getContext();

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (!step_setting.getText().toString().equals("")) {
                    editor.putInt("target", Integer.parseInt(step_setting.getText().toString()));
                    editor.apply();
                    targetstep.setText("" + myPlan.getInt("target", 0) + "");
                }
                if (myPlan.getInt("target", 0) == 0) {
                    targetstep.setText("未设置");
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    /**
     * 开启提醒
     */
    private void startRemind() {
        Toast.makeText(this, "提醒设置成功", Toast.LENGTH_SHORT).show();
        //得到日历实例，主要是为了下面的获取时间
        mCalendar = Calendar.getInstance();
        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();
        //是设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //设置在几点提醒
        mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmTime.getText().toString().split(":")[0]));
        //设置在几分提醒
        mCalendar.set(Calendar.MINUTE, Integer.parseInt(alarmTime.getText().toString().split(":")[1]));
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        //上面设置的就是13点25分的时间点
        //获取上面设置的13点25分的毫秒值
        long selectTime = mCalendar.getTimeInMillis();
        int delaytime;
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            delaytime = 24 * 60 * 60 * 1000 - (int) (systemTime - selectTime);
            Log.i("setalarm", "设置时间小于当前时间");
        } else {
            delaytime = (int) (systemTime - selectTime);
            Log.i("setalarm", "设置时间大于当前时间");
        }
        AlarmService.addNotification(delaytime, "运动提醒", "乐跑圈", "叮叮叮~今天记得完成你的小目标" + myPlan.getInt("target", 0) + "步哦！");

    }

    /**
     * 关闭提醒
     */
    private void stopRemind() {
        AlarmService.cleanAllNotification();
        Toast.makeText(this, "提醒已关闭", Toast.LENGTH_SHORT).show();
    }
}

