package sk.octavia.aasolar;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import rikka.shizuku.Shizuku;
import java.util.Locale;

public class MainActivity extends Activity{
 final int BLUE=0xff24c9ff,WHITE=0xffeef7ff,MUTED=0xff98afc7;
 LinearLayout page;TextView status,plan,access;EditText lat,lon,margin;Button toggle;
 Handler handler=new Handler(Looper.getMainLooper());CancellationSignal locationCancel;
 Shizuku.OnRequestPermissionResultListener permissionListener=(r,g)->refresh();
 Shizuku.OnBinderReceivedListener binderListener=()->runOnUiThread(this::refresh);
 final Runnable update=new Runnable(){public void run(){refresh();handler.postDelayed(this,1500);}};
 int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));return d;}
 TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(7));return t;}
 void label(String s){page.addView(text(s,15,MUTED));}
 Button button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setTextColor(WHITE);b.setTextSize(16);b.setAllCaps(false);b.setBackground(bg(0xff12344f));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(5),0,dp(7));page.addView(b,lp);b.setOnClickListener(v->action.run());return b;}
 EditText field(String title,String value){label(title);EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(WHITE);e.setTextSize(18);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);e.setText(value);page.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));return e;}
 @Override public void onCreate(Bundle state){super.onCreate(state);
  getWindow().setStatusBarColor(0xff071321);getWindow().setNavigationBarColor(0xff071321);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xff071321);
  page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(24),dp(24),dp(24),dp(24));scroll.addView(page);setContentView(scroll);
  TextView title=text("AA Solar",32,WHITE);title.setTypeface(null,Typeface.BOLD);page.addView(title);
  page.addView(text("Day and night for Android Auto maps",18,BLUE));
  page.addView(text("Solar scheduling. Independent of your phone theme.",15,MUTED));
  status=text("",19,WHITE);status.setPadding(dp(16),dp(16),dp(16),dp(16));status.setBackground(bg(0xff102740));page.addView(status);
  plan=text("",17,WHITE);page.addView(plan);
  access=text("",14,MUTED);page.addView(access);
  button("1 · Authorize Shizuku",()->{try{if(!Shizuku.pingBinder()){Intent i=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");if(i!=null)startActivity(i);else startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://shizuku.rikka.app/download/")));return;}if(Shizuku.checkSelfPermission()!=0)Shizuku.requestPermission(10);else toast("Shizuku is authorized");}catch(Exception e){toast(e.getMessage());}});
  button("2 · Use phone location",this::locate);
  lat=field("Latitude",State.prefs(this).getString("lat",""));
  lon=field("Longitude",State.prefs(this).getString("lon",""));
  margin=field("Earlier night / later day (minutes, 0–60)",String.valueOf(State.prefs(this).getInt("margin",15)));
  label("At 15 minutes, night starts 15 minutes before sunset and day starts 15 minutes after sunrise. The saved location is used; refresh it after long trips.");
  toggle=button("",()->{
   if(State.enabled(this)){State.prefs(this).edit().putBoolean("enabled",false).apply();State.cancel(this);State.cancelTest(this);State.log(this,"Automation disabled; AA keeps the last selected mode");refresh();return;}
   if(!save())return;
   if(!Shizuku.pingBinder()||Shizuku.checkSelfPermission()!=0){toast("Start Shizuku and authorize AA Solar first");return;}
   if(!getSystemService(AlarmManager.class).canScheduleExactAlarms()){startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));toast("Allow alarms, then enable automation");return;}
   if(checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},12);
   State.prefs(this).edit().putBoolean("enabled",true).apply();State.schedule(this,false);State.start(this,0);refresh();
  });
  label("Manual test (only changes Android Auto maps)");
  button("☀  Test day mode",()->test(1));button("☾  Test night mode",()->test(2));
  button("Restore Android Auto automatic mode",()->{State.prefs(this).edit().putBoolean("enabled",false).apply();State.cancel(this);State.cancelTest(this);State.start(this,3);refresh();});
  button("View switching log",()->new AlertDialog.Builder(this).setTitle("Switching log").setMessage(State.prefs(this).getString("log","No entries yet")).setPositiveButton("Close",null).show());
  label("Requires Android 13+ and running Shizuku. Restart Shizuku after rebooting your phone. The app operates AA settings on an isolated display, not your physical phone screen. English and Slovak AA settings are supported.");
  Shizuku.addRequestPermissionResultListener(permissionListener);Shizuku.addBinderReceivedListenerSticky(binderListener);refresh();
 }
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 void test(int mode){new AlertDialog.Builder(this).setTitle(mode==1?"Test: day mode":"Test: night mode").setMessage("Switch now or in 20 seconds. Lock your phone during the delayed test to check background operation.").setPositiveButton("Now",(d,w)->State.start(this,mode)).setNeutralButton("In 20 seconds",(d,w)->{try{State.testLater(this,mode);}catch(Exception e){toast(e.getMessage());}}).setNegativeButton("Cancel",null).show();}
 boolean save(){try{
  double a=Double.parseDouble(lat.getText().toString().replace(',','.')),b=Double.parseDouble(lon.getText().toString().replace(',','.'));int m=Integer.parseInt(margin.getText().toString());
  if(!Double.isFinite(a)||!Double.isFinite(b)||a< -90||a>90||b< -180||b>180||m<0||m>60)throw new Exception();
  SunTimes.Plan p=SunTimes.plan(System.currentTimeMillis(),a,b,m);if(p.error!=null){toast(p.error);return false;}
  State.prefs(this).edit().putString("lat",String.valueOf(a)).putString("lon",String.valueOf(b)).putInt("margin",m).apply();return true;
 }catch(Exception e){toast("Enter valid coordinates and a margin of 0–60 minutes");return false;}}
 void locate(){
  if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},11);return;}
  LocationManager lm=getSystemService(LocationManager.class);
  try{
   Location best=null;for(String provider:lm.getProviders(true)){Location l=lm.getLastKnownLocation(provider);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}
   if(best!=null&&System.currentTimeMillis()-best.getTime()<3600000L){useLocation(best);return;}
   if(!lm.isLocationEnabled()){toast("Enable phone location or enter coordinates manually");return;}
   locationCancel=new CancellationSignal();String provider=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)?LocationManager.NETWORK_PROVIDER:LocationManager.FUSED_PROVIDER;
   toast("Getting approximate location…");lm.getCurrentLocation(provider,locationCancel,getMainExecutor(),l->{if(l!=null)useLocation(l);else toast("Location unavailable. Try again or enter coordinates manually.");});
   handler.postDelayed(()->{if(locationCancel!=null)locationCancel.cancel();},15000);
  }catch(Exception e){toast("Could not get location; enter coordinates manually");}
 }
 void useLocation(Location l){lat.setText(String.format(Locale.US,"%.5f",l.getLatitude()));lon.setText(String.format(Locale.US,"%.5f",l.getLongitude()));if(save()){toast("Location saved");if(State.enabled(this)){State.schedule(this,false);State.start(this,0);}refresh();}}
 void refresh(){if(status==null)return;
  boolean active=State.enabled(this);status.setText((active?"● Automation enabled":"○ Automation disabled")+"\n"+State.prefs(this).getString("status","Ready to configure"));
  toggle.setText(active?"Disable automation":"3 · Enable automation");
  boolean ready=false;try{ready=Shizuku.pingBinder()&&Shizuku.checkSelfPermission()==0;}catch(Exception ignored){}
  access.setText("Shizuku: "+(ready?"ready":"start or authorization required")+"\nExact alarms: "+(getSystemService(AlarmManager.class).canScheduleExactAlarms()?"allowed":"not allowed"));
  if(State.located(this)){SunTimes.Plan p=State.plan(this);plan.setText(p.error!=null?p.error:"☀ Sunrise "+State.time(p.rise)+"     ☾ Sunset "+State.time(p.set)+"\nNext switch "+State.time(p.next)+" → "+(p.mode==1?"Night":"Day"));}else plan.setText("Set a location to calculate sunrise and sunset.");
 }
 @Override public void onResume(){super.onResume();handler.post(update);}
 @Override public void onPause(){handler.removeCallbacks(update);super.onPause();}
 @Override public void onDestroy(){Shizuku.removeRequestPermissionResultListener(permissionListener);Shizuku.removeBinderReceivedListener(binderListener);if(locationCancel!=null)locationCancel.cancel();super.onDestroy();}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==11&&g.length>0&&g[0]==0)locate();}
}

