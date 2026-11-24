package sk.octavia.aasolar;
import android.app.*;
import android.content.*;
import android.os.*;
import rikka.shizuku.Shizuku;
import java.util.concurrent.*;

public class SwitchService extends Service{
 static final String CHANNEL="automation";
 final Handler handler=new Handler(Looper.getMainLooper());
 boolean busy;
 static Notification notification(Context c,String text){
  NotificationManager nm=c.getSystemService(NotificationManager.class);
  nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Android Auto switching",NotificationManager.IMPORTANCE_LOW));
  return new Notification.Builder(c,CHANNEL).setSmallIcon(sk.octavia.aasolar.R.drawable.icon).setContentTitle("AA Solar").setContentText(text).setContentIntent(PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE)).build();
 }
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  startForeground(42,notification(this,"Checking map mode"));
  if(busy)return START_NOT_STICKY;busy=true;
  int requested=intent==null?0:intent.getIntExtra("mode",0);
  new Thread(()->execute(requested),"aa-solar-switch").start();return START_NOT_STICKY;
 }
 void execute(int requested){
  PowerManager.WakeLock wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"aasolar:switch");wake.acquire(55000);
  Shizuku.UserServiceArgs args=new Shizuku.UserServiceArgs(new ComponentName(getPackageName(),ShellService.class.getName())).daemon(false).processNameSuffix("solar").version(3);
  CountDownLatch ready=new CountDownLatch(1);IBinder[] remote={null};boolean[] bound={false};
  ServiceConnection connection=new ServiceConnection(){public void onServiceConnected(ComponentName n,IBinder b){remote[0]=b;ready.countDown();}public void onServiceDisconnected(ComponentName n){remote[0]=null;}};
  boolean ok=false;
  try{
   if(requested==0&&!State.enabled(this))return;
   if(!Shizuku.pingBinder()){
    CountDownLatch binderReady=new CountDownLatch(1);Shizuku.OnBinderReceivedListener listener=binderReady::countDown;
    Shizuku.addBinderReceivedListenerSticky(listener);try{binderReady.await(5,TimeUnit.SECONDS);}finally{Shizuku.removeBinderReceivedListener(listener);}
   }
   if(!Shizuku.pingBinder())throw new IllegalStateException("Start Shizuku on your phone");
   if(Shizuku.checkSelfPermission()!=0)throw new IllegalStateException("Authorize AA Solar in Shizuku");
   int mode=requested;
   if(mode==0){if(!State.located(this))throw new IllegalStateException("Set a location first");SunTimes.Plan p=State.plan(this);if(p.error!=null)throw new IllegalStateException(p.error);mode=p.mode;}
   final int selected=mode;
   handler.post(()->{try{Shizuku.bindUserService(args,connection);bound[0]=true;}catch(Exception e){ready.countDown();}});
   if(!ready.await(10,TimeUnit.SECONDS)||remote[0]==null)throw new IllegalStateException("Shizuku did not start the switching service");
   Parcel data=Parcel.obtain(),reply=Parcel.obtain();String result;
   try{data.writeInterfaceToken(ShellService.DESCRIPTOR);data.writeInt(selected);remote[0].transact(IBinder.FIRST_CALL_TRANSACTION,data,reply,0);reply.readException();result=reply.readString();}finally{data.recycle();reply.recycle();}
   if(result==null||!result.startsWith("OK "))throw new IllegalStateException(result);
   String name=mode==1?"Day":mode==2?"Night":"AA automatic mode";
   State.log(this,name+" — verified in Android Auto settings");getSystemService(NotificationManager.class).cancel(43);ok=true;
  }catch(Throwable e){State.log(this,String.valueOf(e.getMessage()));getSystemService(NotificationManager.class).notify(43,notification(this,"Switch failed: "+e.getMessage()));}
  finally{
   if(bound[0])handler.post(()->{try{Shizuku.unbindUserService(args,connection,true);}catch(Exception ignored){}});
   State.schedule(this,!ok);if(wake.isHeld())wake.release();handler.post(()->{stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();});
  }
 }
}
