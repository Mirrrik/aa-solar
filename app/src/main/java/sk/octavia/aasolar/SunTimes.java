package sk.octavia.aasolar;
import java.time.*;

/** NOAA sunrise equation, apparent horizon -0.833 degrees. No network calls. */
public final class SunTimes {
 static double sin(double a){return Math.sin(Math.toRadians(a));}
 static double cos(double a){return Math.cos(Math.toRadians(a));}
 static double wrap(double x,double period){return (x%period+period)%period;}
 public static long event(LocalDate date,double lat,double lon,boolean rise){
  double lngHour=lon/15, t=date.getDayOfYear()+((rise?6:18)-lngHour)/24;
  double m=.9856*t-3.289, l=wrap(m+1.916*sin(m)+.020*sin(2*m)+282.634,360);
  double ra=wrap(Math.toDegrees(Math.atan(.91764*Math.tan(Math.toRadians(l)))),360);
  ra+=(Math.floor(l/90)*90-Math.floor(ra/90)*90);ra/=15;
  double sd=.39782*sin(l),cd=Math.cos(Math.asin(sd));
  double ch=(cos(90.833)-sd*sin(lat))/(cd*cos(lat));
  if(ch>1||ch< -1)return -1;
  double h=Math.toDegrees(Math.acos(ch));if(rise)h=360-h;h/=15;
  double utc=wrap(h+ra-.06571*t-6.622-lngHour,24);
  long result=date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()+Math.round(utc*3600000);
  // Choose the UTC day which corresponds to this event's local solar date.
  long solarNoon=date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()+Math.round((12-lngHour)*3600000);
  while(result<solarNoon-43200000L)result+=86400000L;
  while(result>solarNoon+43200000L)result-=86400000L;
  return result;
 }
 public static final class Plan{
  public int mode;public long next,rise,set;public String error;
 }
 public static Plan plan(long now,double lat,double lon,int margin){
  Plan p=new Plan();LocalDate today=Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate();
  p.rise=event(today,lat,lon,true);p.set=event(today,lat,lon,false);
  if(p.rise<0||p.set<0){p.error="Polar day/night: automation is not supported at this location";return p;}
  long day=p.rise+margin*60000L,night=p.set-margin*60000L;
  if(day>=night){p.error="Margin is too large for this location";return p;}
  p.mode=now>=day&&now<night?1:2;
  p.next=now<day?day:now<night?night:event(today.plusDays(1),lat,lon,true)+margin*60000L;
  if(p.next<=now){p.error="The next sunrise is unavailable";}
  return p;
 }
}
