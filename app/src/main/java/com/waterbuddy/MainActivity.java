package com.waterbuddy;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  SharedPreferences prefs; TextView total, message; Switch voice; LinearLayout page;
  int blue=Color.rgb(48,151,222);
  @Override public void onCreate(Bundle b){super.onCreate(b); prefs=getSharedPreferences("water",MODE_PRIVATE); build(); createChannel(); requestPermission(); schedule(); refresh();}
  TextView label(String x,int size){ TextView v=new TextView(this); v.setText(x); v.setTextSize(size); v.setTextColor(Color.rgb(30,74,101)); v.setPadding(12,12,12,12); return v; }
  Button button(String x){ Button b=new Button(this);b.setText(x);b.setTextSize(17);b.setTextColor(Color.WHITE);b.setBackgroundColor(blue); b.setAllCaps(false); b.setPadding(10,12,10,12); return b; }
  void build(){
    ScrollView scroll=new ScrollView(this); page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(42,55,42,42); page.setBackgroundColor(Color.rgb(234,248,255)); scroll.addView(page); setContentView(scroll);
    TextView title=label("💧 Water Buddy",30); title.setTextColor(blue); page.addView(title); page.addView(latest.setOnClickListener(v -> { ReminderReceiver.showReminder(this); });bel("Your cheerful hydration companion",17));
    total=label("",25); total.setGravity(Gravity.CENTER); total.setPadding(5,45,5,35); page.addView(total);
    Button drink=button("I drank water  +1 glass"); page.addView(drink); drink.setOnClickListener(v->{ addGlass(); });
    message=label("",16); message.setGravity(Gravity.CENTER); page.addView(message);
    Space s=new Space(this);page.addView(s,new LinearLayout.LayoutParams(1,38));
    TextView st=label("Reminder settings",21);page.addView(st);
    voice=new Switch(this); voice.setText("🔊 Cute baby voice reminders"); voice.setTextSize(17); voice.setChecked(prefs.getBoolean("voice",true)); voice.setPadding(8,24,8,24); page.addView(voice); voice.setOnCheckedChangeListener((x,on)->{prefs.edit().putBoolean("voice",on).apply(); Toast.makeText(this,on?"Voice reminders turned on":"Voice reminders turned off",Toast.LENGTH_SHORT).show();});
    page.addView(label("Reminders appear every hour. You can turn the cute voice on or off here, or directly from a reminder notification.",15));
    Button test=button("Test reminder"); page.addView(test); test.setOnClickListener(v -> { ReminderReceiver.showReminder(this); });
  }
  void addGlass(){String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); if(!today.equals(prefs.getString("date","")))prefs.edit().putString("date",today).putInt("glasses",0).apply();prefs.edit().putInt("glasses",prefs.getInt("glasses",0)+1).apply();refresh();Toast.makeText(this,"Yay! Great sip! 💧",Toast.LENGTH_SHORT).show();}
  void refresh(){String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); if(!today.equals(prefs.getString("date","")))prefs.edit().putString("date",today).putInt("glasses",0).apply(); int n=prefs.getInt("glasses",0);total.setText(n+" / 8 glasses today");message.setText(n>=8?"Amazing! Your water goal is complete! 🎉":"Keep going — your body says thank you!");}
  void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel("water","Water reminders",NotificationManager.IMPORTANCE_HIGH);c.setDescription("Friendly hydration reminders");getSystemService(NotificationManager.class).createNotificationChannel(c);}}
  void requestPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},9);}
  void schedule(){AlarmManager a=getSystemService(AlarmManager.class); PendingIntent p=PendingIntent.getBroadcast(this,1,new Intent(this,ReminderReceiver.class).setAction("REMIND"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); a.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+3600000,3600000,p);}
}
