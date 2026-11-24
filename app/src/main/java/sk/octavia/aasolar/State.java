package sk.octavia.aasolar;
import android.content.*;
import android.app.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class State {
 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("solar",0);}
 static boolean enabled(Context c){return prefs(c).getBoolean("enabled",false);}
 static boolean located(Context c){return prefs(c).contains("lat");}
 static SunTimes.Plan plan(Context c){return SunTimes.plan(System.currentTimeMillis(),Double.parseDouble(prefs(c).getString("lat","0")),Double.parseDouble(prefs(c).getString("lon","0")),prefs(c).getInt("margin",15));}
 static String time(long t){return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(t));}
 static void log(Context c,String s){android.util.Log.i("AASolar",s);String line=new SimpleDateFormat("dd.MM. HH:mm:ss",Locale.getDefault()).format(new Date())+"  "+s;String old=prefs(c).getString("log","");prefs(c).edit().putString("status",s).putString("log",(line+"\n"+old).substring(0,Math.min(6000,line.length()+1+old.length()))).apply();}
 static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,0,new Intent(c,AlarmReceiver.class).setAction("sk.octavia.aasolar.TICK"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static void cancel(Context c){((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(alarm(c));}
 static void cancelTest(Context c){c.getSystemService(AlarmManager.class).cancel(PendingIntent.getBroadcast(c,1,new Intent(c,AlarmReceiver.class).setAction("sk.octavia.aasolar.TEST"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));}
 static void schedule(Context c,boolean retry){
  cancel(c);if(!enabled(c)||!located(c))return;
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(!am.canScheduleExactAlarms()){log(c,"Allow exact alarms for reliable switching");return;}
  SunTimes.Plan p=plan(c);if(p.error!=null){log(c,p.error);return;}
  long next=retry?Math.min(p.next,System.currentTimeMillis()+15*60000L):p.next;
  am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,alarm(c));
 }
 static void start(Context c,int mode){c.startForegroundService(new Intent(c,SwitchService.class).putExtra("mode",mode));}
 static void testLater(Context c,int mode){
  AlarmManager am=c.getSystemService(AlarmManager.class);
  if(!am.canScheduleExactAlarms())throw new IllegalStateException("Allow exact alarms first");
  PendingIntent pi=PendingIntent.getBroadcast(c,1,new Intent(c,AlarmReceiver.class).setAction("sk.octavia.aasolar.TEST").putExtra("mode",mode),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+20000,pi);
  log(c,"Test in 20 seconds: "+(mode==1?"Day":"Night")+". You can lock your phone now.");
 }
}
