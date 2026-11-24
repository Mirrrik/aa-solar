package sk.octavia.aasolar;
import android.content.*;
public class AlarmReceiver extends BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){
  if("sk.octavia.aasolar.TEST".equals(i.getAction())){int mode=i.getIntExtra("mode",0);if(mode==1||mode==2)try{State.start(c,mode);}catch(Exception e){State.log(c,"Test: "+e.getMessage());}return;}
  if(!State.enabled(c))return;try{State.schedule(c,false);State.start(c,0);}catch(Exception e){State.log(c,"Automation: "+e.getClass().getSimpleName());State.schedule(c,true);}
 }
}
