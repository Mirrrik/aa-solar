package sk.octavia.aasolar;
import android.os.*;
/** Bound by Shizuku; deliberately exposes only the AA mode enum, no arbitrary commands. */
public class ShellService extends Binder {
 static final String DESCRIPTOR="sk.octavia.aasolar.ISolar";
 public ShellService(){attachInterface(null,DESCRIPTOR);}
 @Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException{
  if(code==16777115){System.exit(0);return true;}
  if(code==INTERFACE_TRANSACTION){reply.writeString(DESCRIPTOR);return true;}
  if(code==FIRST_CALL_TRANSACTION){
   data.enforceInterface(DESCRIPTOR);int mode=data.readInt();data.enforceNoDataAvail();
   try{String result=SolarEngine.run(mode);reply.writeNoException();reply.writeString(result);}
   catch(Throwable e){reply.writeNoException();reply.writeString("ERROR "+e.getClass().getSimpleName()+": "+e.getMessage());}
   return true;
  }return super.onTransact(code,data,reply,flags);
 }
}
