import sk.octavia.aasolar.SunTimes;
import java.time.*;
import java.util.TimeZone;
public class SunTimesTest {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static long at(String date){return LocalDateTime.parse(date).atZone(ZoneId.of("Europe/Bratislava")).toInstant().toEpochMilli();}
 public static void main(String[] args){
  TimeZone.setDefault(TimeZone.getTimeZone("Europe/Bratislava"));
  double lat=48.1486,lon=17.1077;
  for(String date:new String[]{"2026-03-20","2026-06-21","2026-09-23","2026-12-21"}){
   LocalDate d=LocalDate.parse(date);long rise=SunTimes.event(d,lat,lon,true),set=SunTimes.event(d,lat,lon,false);
   check(rise<set,date+" ordered events");
   SunTimes.Plan before=SunTimes.plan(rise+15*60000L-1,lat,lon,15),day=SunTimes.plan(rise+15*60000L,lat,lon,15),night=SunTimes.plan(set-15*60000L,lat,lon,15);
   check(before.mode==2&&day.mode==1&&night.mode==2,"Boundary "+date);
   check(day.next==set-15*60000L&&night.next>set,"Next transition "+date);
   System.out.println(date+" rise="+Instant.ofEpochMilli(rise).atZone(ZoneId.systemDefault()).toLocalTime()+" set="+Instant.ofEpochMilli(set).atZone(ZoneId.systemDefault()).toLocalTime());
  }
  int juneRise=Instant.ofEpochMilli(SunTimes.event(LocalDate.of(2026,6,21),lat,lon,true)).atZone(ZoneId.systemDefault()).getHour();
  check(juneRise==4,"Summer sunrise should be near 04:50");
  check(SunTimes.plan(at("2026-06-21T12:00:00"),89,0,15).error!=null,"Polar location must fail safely");
  System.out.println("Solar boundary tests passed");
 }
}
