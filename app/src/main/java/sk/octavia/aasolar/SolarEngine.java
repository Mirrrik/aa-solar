package sk.octavia.aasolar;

import android.app.UiAutomation;
import android.content.Context;
import android.hardware.display.*;
import android.media.ImageReader;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.accessibility.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import java.util.*;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;

/** Runs only in shell/Shizuku, never in the normal application process. */
public final class SolarEngine {
 static final String AA="com.google.android.projection.gearhead";
 static String normal(CharSequence s){return Normalizer.normalize(s==null?"":s.toString(),Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('\u00a0',' ').trim().toLowerCase(Locale.ROOT);}
 static boolean isSetting(AccessibilityNodeInfo n){String t=normal(n.getText());return t.equals("denny a nocny rezim pre mapy")||t.equals("day/night mode for maps");}
 static AccessibilityNodeInfo find(AccessibilityNodeInfo n,boolean setting,String label){
  if(n==null)return null;
  if(setting?isSetting(n):normal(n.getText()).equals(label))return n;
  for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo r=find(n.getChild(i),setting,label);if(r!=null)return r;}
  return null;
 }
 static boolean click(AccessibilityNodeInfo n){for(int i=0;n!=null&&i<6;i++,n=n.getParent())if(n.isClickable())return n.performAction(AccessibilityNodeInfo.ACTION_CLICK);return false;}
 static AccessibilityNodeInfo find(UiAutomation ua,int id,boolean setting,String label){
  List<AccessibilityWindowInfo> windows=ua.getWindowsOnAllDisplays().get(id);
  if(windows!=null)for(AccessibilityWindowInfo w:windows){AccessibilityNodeInfo root=w.getRoot();if(root!=null&&AA.contentEquals(root.getPackageName())){AccessibilityNodeInfo n=find(root,setting,label);if(n!=null)return n;}}
  return null;
 }
 static AccessibilityNodeInfo waitNode(UiAutomation ua,int id,boolean setting,String label)throws Exception{
  for(int i=0;i<24;i++){AccessibilityNodeInfo n=find(ua,id,setting,label);if(n!=null)return n;Thread.sleep(250);}return null;
 }
 static String[] names(int mode){return mode==1?new String[]{"den","day"}:mode==2?new String[]{"noc","night"}:new String[]{"automaticky","automatic"};}
 static AccessibilityNodeInfo option(UiAutomation ua,int id,int mode)throws Exception{
  for(int i=0;i<12;i++){for(String label:names(mode)){AccessibilityNodeInfo n=find(ua,id,false,label);if(n!=null&&n.isCheckable())return n;}Thread.sleep(200);}return null;
 }
 // Shell supports additional display flags omitted from the public SDK's IntDef.
 @android.annotation.SuppressLint("WrongConstant")
 public static synchronized String run(int mode)throws Exception{
  if(mode<0||mode>3)throw new IllegalArgumentException("Invalid mode");
  if(android.os.Process.myUid()!=2000&&android.os.Process.myUid()!=0)throw new SecurityException("Requires Shizuku shell");
  if(Looper.getMainLooper()==null)Looper.prepareMainLooper();
  Object at=Class.forName("android.app.ActivityThread").getMethod("currentActivityThread").invoke(null);
  if(at==null)at=Class.forName("android.app.ActivityThread").getMethod("systemMain").invoke(null);
  Context context=((Context)at.getClass().getMethod("getSystemContext").invoke(at)).createPackageContext("com.android.shell",0);
  HandlerThread ht=new HandlerThread("aa-solar-frames");ht.start();
  ImageReader reader=null;VirtualDisplay vd=null;UiAutomation ua=null;boolean connected=false;
  try{
   reader=ImageReader.newInstance(720,1280,PixelFormat.RGBA_8888,2);
   reader.setOnImageAvailableListener(r->{android.media.Image image=r.acquireLatestImage();if(image!=null)image.close();},new Handler(ht.getLooper()));
   // Isolated, unlocked display group; no mirroring/capture of the physical phone screen.
   vd=((DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE)).createVirtualDisplay("AA Solar settings",720,1280,240,reader.getSurface(),1|2|8|64|256|1024|2048|4096|8192|16384|32768);
   if(vd==null)throw new IllegalStateException("Auxiliary display unavailable");
   int id=vd.getDisplay().getDisplayId();
   Object connection=Class.forName("android.app.UiAutomationConnection").getConstructor().newInstance();
   ua=(UiAutomation)UiAutomation.class.getConstructor(Looper.class,Class.forName("android.app.IUiAutomationConnection")).newInstance(ht.getLooper(),connection);
   ua.getClass().getMethod("connect",int.class).invoke(ua,1);connected=true;
   AccessibilityServiceInfo info=ua.getServiceInfo();info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;ua.setServiceInfo(info);
   java.lang.Process p=new ProcessBuilder("am","start","--display",String.valueOf(id),"-f","0x18000000","-n",AA+"/.companion.settings.DefaultSettingsActivity").redirectErrorStream(true).start();
   if(!p.waitFor(5,TimeUnit.SECONDS)){p.destroyForcibly();throw new IllegalStateException("AA settings launch timeout");}
   AccessibilityNodeInfo setting=waitNode(ua,id,true,"");
   if(setting==null||!click(setting))throw new IllegalStateException("AA day/night setting not found (English/Slovak supported)");
   int original=0;
   for(int m=1;m<=3;m++){AccessibilityNodeInfo n=option(ua,id,m);if(n!=null&&n.isChecked())original=m;}
   if(original==0)throw new IllegalStateException("Could not verify the current AA mode");
   if(mode==0)return "READ mode="+original;
   AccessibilityNodeInfo wanted=option(ua,id,mode);
   if(wanted==null)throw new IllegalStateException("AA mode option is missing");
   if(original!=mode){
    if(!click(wanted))throw new IllegalStateException("AA rejected the switch");
    Thread.sleep(400);
    setting=waitNode(ua,id,true,"");if(setting==null||!click(setting))throw new IllegalStateException("Could not verify the switch");
    wanted=option(ua,id,mode);if(wanted==null||!wanted.isChecked())throw new IllegalStateException("AA did not confirm the requested mode");
   }
   return "OK mode="+mode+" previous="+original+" verified=true";
  }finally{
   if(ua!=null&&connected)try{ua.getClass().getMethod("disconnect").invoke(ua);}catch(Exception ignored){}
   if(vd!=null)vd.release();if(reader!=null)reader.close();ht.quitSafely();
  }
 }
 public static void main(String[] args)throws Exception{System.out.println(run(args.length==0?0:Integer.parseInt(args[0])));System.exit(0);}
}
