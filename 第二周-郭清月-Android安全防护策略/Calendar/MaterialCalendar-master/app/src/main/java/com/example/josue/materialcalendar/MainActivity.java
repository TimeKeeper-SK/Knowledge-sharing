package com.example.josue.materialcalendar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jmavarez.materialcalendar.CalendarView;
import com.jmavarez.materialcalendar.Interface.OnDateChangedListener;
import com.jmavarez.materialcalendar.Interface.OnMonthChangedListener;
import com.jmavarez.materialcalendar.Util.CalendarDay;
import com.jmavarez.materialcalendar.Util.CalendarUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    TextView dateString;
    View emptyEvents;
    HashSet<CalendarDay> calendarDays;
    // 此函数用于返回比较结果
    public static boolean isOrgApp(Context context, String cert_sha1){
        String current_sha1 = getAppSha1(context);
        return cert_sha1.equals(current_sha1);
    }
    // 此函数用于获取当前APP证书中的sha1值
    public static String getAppSha1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i]).toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();
        // 发布apk时用来签名的keystore中查看到的sha1值，改成自己的
        String cert_sha1 = "A9:87:80:DF:15:E2:B8:07:3D:CE:68:44:7A:B8:08:E4:40:E2:8E:98";
        // 调用isOrgApp()获取比较结果
        boolean is_org_app = isOrgApp(context,cert_sha1);
        // 如果比较初始从证书里查看到的sha1，与代码获取到的当前证书中的sha1不一致，那么就自我销毁
        if(! is_org_app){
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        dateString = (TextView) findViewById(R.id.date);
        emptyEvents = findViewById(R.id.vEmpty);

//        emptyEvents.setVisibility(View.GONE);
        calendarView.setIndicatorsVisibility(true);

        calendarDays = new HashSet<>();
        CalendarDay calendarDay = CalendarDay.from(new Date());

        // Testing Calendar indicators
        for (int i = 1; i < CalendarUtils.getEndOfMonth(calendarDay.getCalendar()) + 1; i++) {
            if (i % 2 == 0) {
                CalendarDay day = CalendarDay.from(i, calendarDay.getMonth() + 1, calendarDay.getYear());
                calendarDays.add(day);
            }
        }

        calendarView.addEvents(calendarDays);

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(Date date) {
                String month = new SimpleDateFormat("MMMM yyyy").format(date);
                setToolbarTitle(month);
            }
        });

        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(Date date) {
                String d = new SimpleDateFormat("dd/MM/yyyy").format(date);
                dateString.setText(d);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarView.reset();
            }
        });

        String date = new SimpleDateFormat("MMMM yyyy").format(calendarView.getDateSelected());
        setToolbarTitle(date);
        String d = new SimpleDateFormat("dd/MM/yyyy").format(calendarView.getDateSelected());
        dateString.setText(d);
    }

    void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

        }

        return super.onOptionsItemSelected(item);
    }
}
