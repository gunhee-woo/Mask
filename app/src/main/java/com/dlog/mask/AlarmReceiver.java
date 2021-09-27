package com.dlog.mask;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {
    private Thread getJsonParserThread;
    String m_Addr = "";
    final String savedBirthEnd = GlobalApplication.prefs.getPreferences();
    private String todayBirthEnd1 ="" ;
    private String todayBirthEnd2 ="";
    private String strBirthEnd = "";


    int nRemainCount = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        strBirthEnd = getbirthEnd();
        Log.d("TAG", "생년끝 문구 : " + strBirthEnd);
        if(!strBirthEnd.equals("주중 미구매자")){//주말이 아니라면
            todayBirthEnd1 = strBirthEnd.substring(7,8);
            Log.d("TAG", "생년끝 1: " + todayBirthEnd1);
            todayBirthEnd2 = strBirthEnd.substring(10,11);
            Log.d("TAG", "생년끝 2: " + todayBirthEnd2);
        }

        GpsTracker gpsTracker = new GpsTracker(context);
        String add = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?";
        String lat = Double.toString(gpsTracker.getLatitude());
        Log.d("TAG", "lat: " +lat);
        String lng = Double.toString(gpsTracker.getLongitude());
        Log.d("TAG", "lng: " +lng);
        String Distance = "1000";
        m_Addr = add + "lat=" + lat + "&lng=" + lng + "&m=" + Distance;

        getJsonParserThread = new Thread(new getJsonParser());
        getJsonParserThread.start();
        try {
            getJsonParserThread.join();
        } catch (Exception e) {
            String exce = e.toString();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");


        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            String channelName ="매일 알람 채널";
            String description = "매일 정해진 시간에 알람합니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        // 노티피케이션 동작시킴
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH");
        String getTime = simpleDate.format(mDate);
        int currTime = Integer.parseInt(getTime);
        Log.d("TAG", "currTime: " + currTime);

        CharSequence cs = "오늘은 "+strBirthEnd+"이신분이 마스크 구매가능합니다\n"
                +currTime+"시 현재 "+"반경 1km이내 마스크 구입가능 한 판매점이 "
                + String.valueOf(nRemainCount) + "곳 있습니다";

        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.icon))
                .setTicker("{Time to watch some cool stuff!}")
                .setContentTitle("마스크 있어요!")
                .setContentText("1km 이내 구입가능한 판매점이  " + String.valueOf(nRemainCount) + "곳 있어요")//1km 이내 구입가능한 판매점이 OO곳 있어요
                .setContentInfo("마스크 있어요")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cs))
                .setContentIntent(pendingI);

        if (notificationManager != null) {



            Calendar nextNotifyTime = Calendar.getInstance();
            if((Integer.parseInt(getTime)>=0) && (Integer.parseInt(getTime)<6) ){
                //울리면 안됨 금일 7시로 설정.
                Log.d("TAG","0~7시 알림 x" + getTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 7);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                nextNotifyTime = calendar;
            }
            else if((Integer.parseInt(getTime)>20)){
                //울리면 안됨 다음날 7시ㅣ로 설정.
                Log.d("TAG","20시 이후 알림 x" + getTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.DATE,1);
                calendar.set(Calendar.HOUR_OF_DAY, 7);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                nextNotifyTime = calendar;
            }
            else {
                // 한시간 뒤로 알람시간 결정
                Log.d("TAG","마스크 구매가능한 곳 :" + nRemainCount + "개");
                if((nRemainCount >= 5) && ((savedBirthEnd.equals(todayBirthEnd1) || (savedBirthEnd.equals(todayBirthEnd2))))
                        || (strBirthEnd.equals("주중 미구매자"))) {//5곳 이상일경우 , 생년끝자리가 맞을 경우, 주말인 경우 알람
                    notificationManager.notify(1234, builder.build());
                    Log.d("TAG","Noti Time" + getTime);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                nextNotifyTime = calendar;
            }



            //  Preference에 설정한 값 저장
            SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
            editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
            editor.apply();


            Boolean dailyNotify = true; // 무조건 알람을 사용

            PackageManager pm = context.getPackageManager();
            ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


            // 사용자가 매일 알람을 허용했다면
            if (dailyNotify) {

                if (alarmManager != null) {

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextNotifyTime.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, pendingIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextNotifyTime.getTimeInMillis(), pendingIntent);
                    }
                }

                // 부팅 후 실행되는 리시버 사용가능하게 설정
                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);

            }

        }
    }
    public class getJsonParser implements Runnable {
        public void run() {
            nRemainCount = 0;

            URL url;
            String strUrl = m_Addr;

            try
            {
                url = new URL(strUrl);
                InputStreamReader isr = new InputStreamReader(url.openConnection().getInputStream(), "UTF-8");

                BufferedReader br = new BufferedReader(isr);
                String strTotal = "";
                while(true)
                {
                    String strResult = br.readLine();
                    if (strResult == null)
                        break;
                    strTotal += strResult.trim();
                }

                JSONObject jsonObject = new JSONObject(strTotal);
                int nCount = jsonObject.getInt("count");
                JSONArray arr = jsonObject.getJSONArray("stores");
                for (int i = 0; i < nCount; i++)
                {
                    JSONObject object = arr.getJSONObject(i);
                    /*String straddr = object.getString("addr");
                    String strCode = object.getString("code");
                    String strCreate = object.getString("created_at");
                    double dLat = object.getDouble("lat");
                    double dLng = object.getDouble("lng");*/
                    String strName = object.getString("name");

                    String strRemain = object.getString("remain_stat");

                    if(!strRemain.equals("empty")&&!(strRemain.equals("break"))) {
                        Log.d("TAG", "약국 이름: " + strName);
                        nRemainCount++;
                    }
                    /*String strStock = object.getString("stock_at");
                    String strType = object.getString("type");*/


                }
            }
            catch (Exception e)
            {
                String ex = e.toString();
            }
        }

    }
    private String getbirthEnd(){
        Calendar cal = Calendar.getInstance();
        String strbirthEnd = null;
        int nWeek = cal.get(Calendar.DAY_OF_WEEK);
        if(nWeek == 1){
            strbirthEnd = "주중 미구매자";
        }else if(nWeek == 2){
            strbirthEnd = "생년 끝자리 1, 6";
        }else if(nWeek == 3){
            strbirthEnd = "생년 끝자리 2, 7";
        }else if(nWeek == 4){
            strbirthEnd = "생년 끝자리 3, 8";
        }else if(nWeek == 5){
            strbirthEnd = "생년 끝자리 4, 9";
        }else if(nWeek == 6){
            strbirthEnd = "생년 끝자리 5, 0";
        }else if(nWeek == 7){
            strbirthEnd = "주중 미구매자";
        }
        return strbirthEnd;
    }

}
