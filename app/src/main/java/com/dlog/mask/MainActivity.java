package com.dlog.mask;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.dlog.mask.views.BoundInfoView;
import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener{
    private MapView mapView;
    private ArrayList<PharmacyData> arrPharmacyData = new ArrayList<PharmacyData>();

    private static final String LOG_TAG = "MainActivity";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private Thread getJsonParserThread;
    String Distance = "1000";
    String m_Addr = "";
    TextView purchaseEnableBirthTexView;
    TextView purchaseEnableBirth1920TexView;
    TextView purchaseEnableStoreTextView;
    TextView purchaseEnableStoreCountTextView;
    TextView maskSlideInfo;
    GridView markerGridView;
    GridAdapter gridAdapter;

    Calendar cal;
    int storeSum = 0;
    int nPlentyCount = 0;
    int nSomeCount = 0;
    int nFewCount = 0;
    int nEmptyCount = 0;

    boolean[] selectMarker = {true, true, true, true};

    com.facebook.ads.AdView adView = null;

    private ConstraintLayout birth_popup_layout;
    private Button btn_birth_popup;
    private EditText edt_birth_popup;

    private Location centerLocation;
    private boolean isLayoutSetted = false;

    private MapPOIItem[] locateMapPOIItems;
    private int DEFAULT_KM = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //default 반경 setting

        final String savedKM = GlobalApplication.prefs.getDefaultKMPreferences();
        TextView txt_bound = findViewById(R.id.txt_bound_info);
        if(savedKM.equals("")){//사용자가 따로 기본 반경을 저장하지 않았다 >  디폴트 값인 1km 사용
            txt_bound.setText("1km");
        }
        else{
            txt_bound.setText(savedKM+"km");
            DEFAULT_KM = Integer.parseInt(savedKM);
            Distance = String.valueOf(Integer.parseInt(savedKM)*1000);

        }
        //생년 끝자리 입력 받기
        final String savedBirthEnd = GlobalApplication.prefs.getPreferences();
        if(savedBirthEnd.equals("")) {//저장된 생년 끝자리 없음 > 생년 끝자리 받기
            birth_popup_layout = findViewById(R.id.birth_end_popup_layout);
            btn_birth_popup = findViewById(R.id.btn_birth_end_popup);
            edt_birth_popup = findViewById(R.id.edt_birth_end);
            birth_popup_layout.setVisibility(View.VISIBLE);

            edt_birth_popup.setOnEditorActionListener(new TextView.OnEditorActionListener(){

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(v.getId() == R.id.edt_birth_end && actionId == EditorInfo.IME_ACTION_DONE){
                        //키보드 내리기
                        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edt_birth_popup.getWindowToken(),0);
                    }
                    return false;
                }
            });

            btn_birth_popup.setOnClickListener(v -> {
                if(edt_birth_popup.length() != 4){
                    Toast.makeText(getApplicationContext(),"출생년도를 정확히 입력해주세요!",Toast.LENGTH_LONG).show();
                }
                else{
                    GlobalApplication.prefs.savePreferences(edt_birth_popup.getText().toString().substring(3));//맨끝자리만 저장.
                    Log.d("TAG","prefs.getPreferences : " + GlobalApplication.prefs.getPreferences());
                    setAlarm();
                    birth_popup_layout.setVisibility(View.GONE);
                    setNoticeBtnEvent();
                    if(GlobalApplication.prefs.getIsNewPreferences().equals("")) {//공지를 DB에 저장한적이 없다면 저장
                        savedNotices();
                    }
                    else{
                        //Db에서 공지 가져와서 띄우기
                        new NoticeDbAsyncTask(this,null,2).execute();
                    }
                    //키보드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_birth_popup.getWindowToken(),0);
                }
            });
        }
        else{
            setAlarm();
            setNoticeBtnEvent();
            if(GlobalApplication.prefs.getIsNewPreferences().equals("")) {//공지를 DB에 저장한적이 없다면 저장
                savedNotices();
            }
            else{
                //Db에서 공지 가져와서 띄우기
                new NoticeDbAsyncTask(this,null,2).execute();
            }
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        int nStatusBarHeight = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            nStatusBarHeight = getResources().getDimensionPixelSize(resId);
        }

        final DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        int nTotalWidth = metrics.widthPixels;
        int nTotalHeight = metrics.heightPixels - nStatusBarHeight;
        double fTotalPerWidth = nTotalWidth / 100.0;
        double fTotalPerHeight = nTotalHeight / 100.0;

        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        ImageView imgAD = (ImageView) findViewById(R.id.imgAD);

        double dValue = Math.random();
        int iValue = (int) (Math.round(dValue * 9));

        if (iValue == 0) {
            adContainer.setVisibility(View.GONE);

            LinearLayout.LayoutParams imgADParam = new LinearLayout.LayoutParams((int) (fTotalPerWidth * 90), (int) (fTotalPerWidth * 90 * 0.1666));
            imgADParam.setMargins((int) (fTotalPerWidth * 5), 0, 0, 0);
            imgAD.setLayoutParams(imgADParam);

            imgAD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dlog.ailotto")));
                }
            });
        } else {
            imgAD.setVisibility(View.GONE);

            AudienceNetworkAds.initialize(this);

            adView = new com.facebook.ads.AdView(this, getString(R.string.facebook_ad_id), AdSize.BANNER_HEIGHT_50);

            adContainer.addView(adView);

            adView.loadAd();
        }

        mapView = new MapView(this);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(mapPOIEventListener);
        mapView.setMapViewEventListener(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        //위치정보 퍼미션 허용
        try {
            if (!checkLocationServicesStatus()) {
                showDialogForLocationServiceSetting();
            } else {
                checkRunTimePermission();
            }
        } catch (Exception e) {
            String exce = e.toString();
        }
        //현위치 버튼 setting
        ImageView myLocationImg = findViewById(R.id.img_my_location);
        myLocationImg.setOnClickListener(v -> {
            moveToCurrentLocation();
        });
        //설정 뷰 setting
        ImageView settingImg = findViewById(R.id.img_setting);
        ConstraintLayout setting_layout = findViewById(R.id.layout_setting);
        setting_layout.setOnClickListener(v -> {

        });
        ConstraintLayout setting_back = findViewById(R.id.layout_setting_back);
        setting_back.setOnClickListener(v -> {
            setting_back.setVisibility(View.INVISIBLE);
        });
        settingImg.setOnClickListener(v -> {
            Animation ani = AnimationUtils.loadAnimation(this,R.anim.setting_slide);
            ani.setDuration(800);
            setting_back.setVisibility(View.VISIBLE);
            setting_layout.startAnimation(ani);
            setting_layout.setVisibility(View.VISIBLE);
        });
        ImageView escImg = setting_layout.findViewById(R.id.img_setting_esc);
        escImg.setOnClickListener(v -> {
            setting_back.setVisibility(View.INVISIBLE);
            setting_layout.setVisibility(View.INVISIBLE);
        });
        TextView txtBirth = setting_layout.findViewById(R.id.txt_setting_birth);
        txtBirth.setOnClickListener(v -> {
            setting_back.setVisibility(View.INVISIBLE);
            setting_layout.setVisibility(View.INVISIBLE);
            birth_popup_layout = findViewById(R.id.birth_end_popup_layout);
            btn_birth_popup = findViewById(R.id.btn_birth_end_popup);
            edt_birth_popup = findViewById(R.id.edt_birth_end);
            birth_popup_layout.setVisibility(View.VISIBLE);
            btn_birth_popup.setOnClickListener(v2 -> {
                if(edt_birth_popup.length() != 4){
                    Toast.makeText(getApplicationContext(),"출생년도를 정확히 입력해주세요!",Toast.LENGTH_LONG).show();
                }
                else{
                    GlobalApplication.prefs.savePreferences(edt_birth_popup.getText().toString().substring(3));//맨끝자리만 저장.
                    Log.d("TAG","prefs.getPreferences : " + GlobalApplication.prefs.getPreferences());
                    setAlarm();
                    birth_popup_layout.setVisibility(View.GONE);
                    //키보드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_birth_popup.getWindowToken(),0);
                }
            });
        });

        //설정 > 기본 반경 변경
        TextView txt_setting_bound = setting_layout.findViewById(R.id.txt_setting_bound);
        ConstraintLayout bound_setting_layout = findViewById(R.id.bound_setting_popup_layout);
        NumberPicker numPicker = bound_setting_layout.findViewById(R.id.numpicker_setting);
        numPicker.setMinValue(1);
        numPicker.setMaxValue(3);
        Button btn_bound_setting_ok = bound_setting_layout.findViewById(R.id.btn_bound_setting);
        btn_bound_setting_ok.setOnClickListener(v1 -> {
            switch (numPicker.getValue()){
                case 1 : {
                    GlobalApplication.prefs.saveDefaultKMPreferences("1");
                    break;
                }
                case 2 : {
                    GlobalApplication.prefs.saveDefaultKMPreferences("2");
                    break;
                }
                case 3 : {
                    GlobalApplication.prefs.saveDefaultKMPreferences("3");
                    break;
                }
            }
            bound_setting_layout.setVisibility(View.GONE);
        });
        txt_setting_bound.setOnClickListener(v -> {
            setting_back.setVisibility(View.INVISIBLE);
            setting_layout.setVisibility(View.INVISIBLE);
            bound_setting_layout.setVisibility(View.VISIBLE);
        });


    }

    private MapView.POIItemEventListener mapPOIEventListener = new MapView.POIItemEventListener() {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
            setMaskSlideInfo(mapPOIItem);
        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        }

        @Override
        public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        }
    };

    public String calTakeTime(float distance) {
        // 1시간 -> 4km => 1분 -> 66.66666m
        int time = (int)distance / 67;
        return "도보 약" + String.valueOf(time) + "분";
    }

    public void setMaskSlideInfo(MapPOIItem mapPOIItem) {
        maskSlideInfo = (TextView) findViewById(R.id.slide_info);
        maskSlideInfo.setSingleLine();
        maskSlideInfo.setSelected(true);
        maskSlideInfo.setHorizontallyScrolling(true);
        maskSlideInfo.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        for(int i = 0; i < arrPharmacyData.size(); i++) {
            if(arrPharmacyData.get(i).getStrName().equals(mapPOIItem.getItemName())) {
                if (discardLatLng(arrPharmacyData.get(i).getdLat()) == discardLatLng(mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude)
                        && discardLatLng(arrPharmacyData.get(i).getdLng()) == discardLatLng(mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude)) {
                    String slideText = "        " + arrPharmacyData.get(i).getStrName() + "("
                            + calTakeTime(arrPharmacyData.get(i).getDistance_to_curr_location()) + ") "
                            + arrPharmacyData.get(i).getStraddr();
                    maskSlideInfo.setText(slideText.replace(" ", "\u00A0"));
                }
            }
        }
    }

    public double discardLatLng(Double latlng) {
        return (int)(latlng * 10000) / 10000.0; // 소수점 넷째자리 미만 버림
    }

    public void setCurrentMarker() {
        double dLatitude = centerLocation.getLatitude(); // 지도의 중심 위도
        double dLongitude = centerLocation.getLongitude(); // 지도의 중심 경도
        MapPoint currentMapPoint = MapPoint.mapPointWithGeoCoord(dLatitude, dLongitude);
        MapPOIItem currentMarker = new MapPOIItem();
        currentMarker.setItemName("현위치");
        currentMarker.setShowDisclosureButtonOnCalloutBalloon(false);
        currentMarker.setMapPoint(currentMapPoint);
        currentMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mapView.addPOIItem(currentMarker);

        if(!isLayoutSetted) {
            switch (DEFAULT_KM){
                case 1:{
                    mapView.setZoomLevel(4, false);
                    break;
                }
                case 2:{
                    mapView.setZoomLevel(5, false);
                    break;
                }
                case 3:{
                    mapView.setZoomLevel(6, false);
                    break;
                }
            }
            mapView.setMapCenterPoint(currentMapPoint,false);
        }
    }

    public void setLayout() {
        double dLatitude = centerLocation.getLatitude(); // 지도의 중심 위도
        double dLongitude = centerLocation.getLongitude(); // 지도의 중심 경도
        String add = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?";
        // 원으로 표시
        m_Addr = add + "lat=" + String.valueOf(dLatitude) + "&lng=" + String.valueOf(dLongitude) + "&m=" + Distance;

        //현 위치 마커
        setCurrentMarker();

        // Color.argb(128, 255, 0, 0)
        MapCircle circle1 = new MapCircle(
                MapPoint.mapPointWithGeoCoord(dLatitude, dLongitude), // center
                Integer.parseInt(Distance), // radius
                Color.BLUE,
                Color.argb(100,187,222,251)// strokeColor) // fillColor

        );
        circle1.setTag(1234);
        mapView.addCircle(circle1);


        getJsonParserThread = new Thread(new MainActivity.getJsonParser());
        getJsonParserThread.start();
        try {
            getJsonParserThread.join();
        } catch (Exception e) {
            String exce = e.toString();
        }
        if(arrPharmacyData.size() != 0) {
            //마스크 재고가 많으면서 가장 가까운 지점 찾기 위한 변수
            int position = 0;
            int color = 0; // 0 : empty,break  1 : few   2 : some   3 : plenty
            Log.d("TAG", "arrPharmacyData size" + ": " + arrPharmacyData.size());
            for (int i = 0; i < arrPharmacyData.size(); i++) {
                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(arrPharmacyData.get(i).getdLat(), arrPharmacyData.get(i).getdLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(arrPharmacyData.get(i).getStrName());
                marker.setMapPoint(mapPoint);
                marker.setShowDisclosureButtonOnCalloutBalloon(false);
                setStoreType(arrPharmacyData.get(i).getStrType(), marker);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                if (arrPharmacyData.get(i).getStrRemain().equals("plenty")) {
                    storeSum++;
                    nPlentyCount++;
                    marker.setCustomImageResourceId(R.drawable.marker_green);
                    marker.setCustomSelectedImageResourceId(R.drawable.marker_green_big);
                    marker.setTag(1);
                    Log.d("TAG", "plenty: " + arrPharmacyData.get(i).getStrName());
                } else if (arrPharmacyData.get(i).getStrRemain().equals("some")) {
                    storeSum++;
                    nSomeCount++;
                    marker.setCustomImageResourceId(R.drawable.marker_yellow);
                    marker.setCustomSelectedImageResourceId(R.drawable.marker_yellow_big);
                    marker.setTag(2);
                    Log.d("TAG", "some: " + arrPharmacyData.get(i).getStrName());
                } else if (arrPharmacyData.get(i).getStrRemain().equals("few")) {
                    storeSum++;
                    nFewCount++;
                    marker.setCustomImageResourceId(R.drawable.marker_red);
                    marker.setCustomSelectedImageResourceId(R.drawable.marker_red_big);
                    marker.setTag(3);
                    Log.d("TAG", "few: " + arrPharmacyData.get(i).getStrName());
                } else if (arrPharmacyData.get(i).getStrRemain().equals("empty") || arrPharmacyData.get(i).getStrRemain().equals("break")) {
                    nEmptyCount++;
                    marker.setCustomImageResourceId(R.drawable.marker_gray);
                    marker.setCustomSelectedImageResourceId(R.drawable.marker_gray_big);
                    marker.setTag(4);
                    Log.d("TAG", "empty or break: " + arrPharmacyData.get(i).getStrName());
                }
                mapView.addPOIItem(marker);
                //현재위치까지 거리 계산
                Location curLocation = new Location("current Location");
                curLocation.setLatitude(dLatitude);
                curLocation.setLongitude(dLongitude);
                Location desLocation = new Location("dsetination Location");
                desLocation.setLatitude(arrPharmacyData.get(i).getdLat());
                desLocation.setLongitude(arrPharmacyData.get(i).getdLng());
                float distance = curLocation.distanceTo(desLocation);
                arrPharmacyData.get(i).setDistance_to_curr_location(distance);
                //    color        >>>>>>      0 : empty,break     1 : few      2 : some       3 : plenty
                //마스크 재고가 많으면서 가장 가까운 지점 찾기
                switch (arrPharmacyData.get(i).getStrRemain()) {
                    case "plenty": {
                        if (color == 3) {
                            if (arrPharmacyData.get(position).getDistance_to_curr_location() >= arrPharmacyData.get(i).getDistance_to_curr_location()) {
                                position = i;
                            }
                        } else {
                            position = i;
                            color = 3;
                        }
                        break;
                    }
                    case "some": {
                        if (color != 3) {//녹색(plenty)가 최우선이기 때문에.
                            if (color == 2) {
                                if (arrPharmacyData.get(position).getDistance_to_curr_location() >= arrPharmacyData.get(i).getDistance_to_curr_location()) {
                                    position = i;
                                }
                            } else {
                                position = i;
                                color = 2;
                            }
                        }
                        break;
                    }
                    case "few": {
                        if ((color != 3) && (color != 2)) {
                            if (color == 1) {
                                if (arrPharmacyData.get(position).getDistance_to_curr_location() >= arrPharmacyData.get(i).getDistance_to_curr_location()) {
                                    position = i;
                                }
                            } else {
                                position = i;
                                color = 1;
                            }
                        }
                        break;
                    }
                    case "empty":
                    case "break": {
                        if (color == 0) {
                            if (arrPharmacyData.get(position).getDistance_to_curr_location() >= arrPharmacyData.get(i).getDistance_to_curr_location()) {
                                position = i;
                                color = 0;
                            }
                        }
                        break;
                    }
                }
            }
            //arrPharmacyData.get(position) 이 가장 마스크 재고량이 많으면서 가까운 지점 입니다.
            Log.d("TAG", "가장 마스크 재고량이 많으면서 가까운 지점: " + arrPharmacyData.get(position).getStrName());
            MapPOIItem[] pois = mapView.getPOIItems();
            mapView.selectPOIItem(pois[position+1], true);
            setMaskSlideInfo(pois[position+1]);
            locateMapPOIItems = mapView.getPOIItems();
        }

        purchaseEnableBirthTexView = (TextView) findViewById(R.id.purchase_enable_birth);
        purchaseEnableBirth1920TexView = (TextView) findViewById(R.id.purchase_enable_birth1920);
        purchaseEnableBirthTexView.setText("오늘 구입가능 출생연도:  ");
        cal = Calendar.getInstance();
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
            case 7:
                purchaseEnableBirth1920TexView.setText("주중 미구매자 전원");
                break;
            case 2:
                purchaseEnableBirth1920TexView.setText("19□1  20□1" + "\r\n" + "19□6  20□6");
                break;
            case 3:
                purchaseEnableBirth1920TexView.setText("19□2  20□2" + "\r\n" + "19□7  20□7");
                break;
            case 4:
                purchaseEnableBirth1920TexView.setText("19□3  20□3" + "\r\n" + "19□8  20□8");
                break;
            case 5:
                purchaseEnableBirth1920TexView.setText("19□4  20□4" + "\r\n" + "19□9  20□9");
                break;
            case 6:
                purchaseEnableBirth1920TexView.setText("19□5  20□5" + "\r\n" + "19□0  20□0");
                break;
        }

        purchaseEnableStoreTextView = (TextView) findViewById(R.id.purchase_enable_store);
        purchaseEnableStoreCountTextView = (TextView) findViewById(R.id.purchase_enable_store_count);
        purchaseEnableStoreTextView.setText("지금 구입가능 판매지점:  ");
        purchaseEnableStoreCountTextView.setText(String.valueOf(storeSum) + "개소");

        gridAdapter = new GridAdapter(getApplicationContext(), R.layout.grid_item);
        gridAdapter.addMarkerData("많음(100개 이상): " + String.valueOf(nPlentyCount), R.drawable.marker_green_big);
        gridAdapter.addMarkerData("보통(30 ~ 99개): " + String.valueOf(nSomeCount), R.drawable.marker_yellow_big);
        gridAdapter.addMarkerData("부족(2 ~ 29개): " + String.valueOf(nFewCount), R.drawable.marker_red_big);
        gridAdapter.addMarkerData("품절: " + String.valueOf(nEmptyCount), R.drawable.marker_gray_big);
        markerGridView = (GridView) findViewById(R.id.marker_info);
        markerGridView.setAdapter(gridAdapter);

        markerGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    createMarker(position);
                } else if(position == 1) {
                    createMarker(position);
                } else if(position == 2) {
                    createMarker(position);
                } else if(position == 3) {
                    createMarker(position);
                }
            }
        });

        /*
        Spinner bound_spinner = findViewById(R.id.spinner_bound);
        bound_spinner.setAdapter(ArrayAdapter.createFromResource(this,R.array.map_bound,R.layout.bound_spinner_item));*/
        BoundInfoView boundView = findViewById(R.id.bound_info_view);
        boundView.setOnClickListener(v -> {
            TextView txtBoundInfo = boundView.findViewById(R.id.txt_bound_info);
            switch (txtBoundInfo.getText().toString()){
                case "1km" :{
                    boundView.setClickable(false);
                    txtBoundInfo.setText("2km");
                    mapView.setZoomLevel(5, false);
                    Distance = "2000";
                    cleanData();
                    setLayout();
                    boundView.setClickable(true);
                    break;
                }
                case "2km" :{
                    boundView.setClickable(false);
                    txtBoundInfo.setText("3km");
                    mapView.setZoomLevel(6, false);
                    Distance = "3000";
                    cleanData();
                    setLayout();

                    boundView.setClickable(true);
                    break;
                }
                case "3km" :{
                    boundView.setClickable(false);
                    txtBoundInfo.setText("1km");
                    mapView.setZoomLevel(4, false);
                    Distance = "1000";
                    cleanData();
                    setLayout();
                    boundView.setClickable(true);
                    break;
                }

            }
        });
        isLayoutSetted = true;
    }

    public void setStoreType(String type, MapPOIItem marker) {
        switch (type) {
            case "01":
                marker.setLeftSideButtonResourceIdOnCalloutBalloon(R.drawable.pharmacy);
                break;
            case "02":
                marker.setLeftSideButtonResourceIdOnCalloutBalloon(R.drawable.postoffice);
                break;
            case "03":
                marker.setLeftSideButtonResourceIdOnCalloutBalloon(R.drawable.nh);
                break;
        }
    }

    public void changeMarker(int pos) {
        MapPOIItem[] mapPOIItems = locateMapPOIItems;
        mapView.removeAllPOIItems();
        for(int i = 0; i < mapPOIItems.length; i++) {
            if(mapPOIItems[i].getTag() == 1 && pos == 0) {
                MapPOIItem marker = initMarker(mapPOIItems[i]);
                marker.setCustomImageResourceId(R.drawable.marker_green);
                marker.setCustomSelectedImageResourceId(R.drawable.marker_green_big);
                marker.setTag(1);
                mapView.addPOIItem(marker);
            } else if(mapPOIItems[i].getTag() == 2 && pos == 1) {
                MapPOIItem marker = initMarker(mapPOIItems[i]);
                marker.setCustomImageResourceId(R.drawable.marker_yellow);
                marker.setCustomSelectedImageResourceId(R.drawable.marker_yellow_big);
                marker.setTag(2);
                mapView.addPOIItem(marker);
            } else if(mapPOIItems[i].getTag() == 3 && pos == 2) {
                MapPOIItem marker = initMarker(mapPOIItems[i]);
                marker.setCustomImageResourceId(R.drawable.marker_red);
                marker.setCustomSelectedImageResourceId(R.drawable.marker_red_big);
                marker.setTag(3);
                mapView.addPOIItem(marker);
            } else if(mapPOIItems[i].getTag() == 4 && pos == 3) {
                MapPOIItem marker = initMarker(mapPOIItems[i]);
                marker.setCustomImageResourceId(R.drawable.marker_gray);
                marker.setCustomSelectedImageResourceId(R.drawable.marker_gray_big);
                marker.setTag(4);
                mapView.addPOIItem(marker);
            }
        }
        setCurrentMarker();
        mapView.setDefaultCurrentLocationMarker();
    }

    public MapPOIItem initMarker(MapPOIItem mapPOIItem) {
        MapPOIItem marker = new MapPOIItem();
        marker.setMapPoint(mapPOIItem.getMapPoint());
        marker.setItemName(mapPOIItem.getItemName());
        marker.setShowDisclosureButtonOnCalloutBalloon(false);
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setLeftSideButtonResourceIdOnCalloutBalloon(mapPOIItem.getLeftSideButtonResourceIdOnCalloutBalloon());
        return marker;
    }

    public void createMarker(int pos) {
        if(selectMarker[pos]) {
            changeMarker(pos);
            selectMarker[pos] = false;
            initSelectMarker(pos);
        } else {
            initMarkerInfo();
            selectMarker[pos] = true;
        }
        maskSlideInfo.setText("");
    }

    public void initSelectMarker(int pos) {
        for(int i = 0; i < selectMarker.length; i++) {
            if(i != pos)
                selectMarker[i] = true;
        }
    }

    public void initMarkerInfo() {
        mapView.addPOIItems(locateMapPOIItems);
    }

    @Override
    protected void onDestroy() {
        if(adView != null) {
            adView.destroy();
        }

        super.onDestroy();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        //Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }
    private void onFinishReverseGeoCoding(String result) {
        Toast.makeText(MainActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }
    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음
                GpsTracker gpsTracker = new GpsTracker(this);
                centerLocation = new Location("Current Center Point");
                centerLocation.setLongitude(gpsTracker.longitude);
                centerLocation.setLatitude(gpsTracker.latitude);
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
                setLayout();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
            GpsTracker gpsTracker = new GpsTracker(this);
            centerLocation = new Location("Current Center Point");
            centerLocation.setLongitude(gpsTracker.longitude);
            centerLocation.setLatitude(gpsTracker.latitude);
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
            setLayout();
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.d("TAG","on Map Initialized");
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        Log.d("Tap", "onMapViewSingleTapped:  long  "+mapPoint.getMapPointGeoCoord().longitude);
        Log.d("Tap", "onMapViewSingleTapped:  lat  "+mapPoint.getMapPointGeoCoord().latitude);
        maskSlideInfo.setText("");
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        Log.d("TAG","on Map long pressed");
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        Log.d("TAG","on Map drag started");
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        Log.d("TAG","on Map drag ended");
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        Log.d("TAG","on Map move finished");
        Log.d("TAG","Map Point  lat:" + mapPoint.getMapPointGeoCoord().latitude);
        Log.d("TAG","Map Point  long:" + mapPoint.getMapPointGeoCoord().longitude);
        Double lat = mapPoint.getMapPointGeoCoord().latitude;
        Double lng = mapPoint.getMapPointGeoCoord().longitude;
        Location desLocation = new Location("dsetination Location");
        desLocation.setLatitude(lat);
        desLocation.setLongitude(lng);
        float distance = centerLocation.distanceTo(desLocation);
        if(distance > Float.valueOf(Distance)){
            //반경 업데이트
            centerLocation.setLatitude(lat);
            centerLocation.setLongitude(lng);
            cleanData();
            setLayout();
        }
        if(!isLayoutSetted){
            setLayout();
        }

    }

    public class getJsonParser implements Runnable {
        public void run() {
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
                int nCount = jsonObject.getInt("count"); // 약국수
                JSONArray arr = jsonObject.getJSONArray("stores");
                HashMap<String,Integer> map = new HashMap<>();
                Double min = 0.0;
                Double max = 360.0;
                Random randomGenerator = new Random();

                for (int i = 0; i < nCount; i++)
                {
                    JSONObject object = arr.getJSONObject(i);
                    String straddr = object.getString("addr"); // 약국주소
                    String strCode = object.getString("code");
                    String strCreate = object.getString("created_at");
                    double dLat = object.getDouble("lat"); //위도
                    double dLng = object.getDouble("lng"); // 경도
                    String str = String.valueOf(dLat)+String.valueOf(dLng);
                    String strName = object.getString("name");
                    if(map.containsKey(str)){
                        Log.d("Geo", "name  : " + strName);
                        Log.d("Geo", "dLat: " + dLat);
                        Log.d("Geo", "dLng: " + dLng);
                        //위도 경도 수정
                        double randomDouble = randomGenerator.nextDouble();
                        double randVal = min + randomDouble * (max - min) ;
                        Log.d("Geo", "rand Value: " + randVal);
                        dLat = getLat(dLat,0.00005,randVal);
                        Log.d("Geo", "getLat: " + dLat);
                        dLng = getlng(dLng,0.00005,randVal);
                        Log.d("Geo", "getLng: " + dLng);
                        String str2 = String.valueOf(dLat)+String.valueOf(dLng);
                        map.put(str2,1);
                    }
                    else{
                        map.put(str,1);
                    }
                    if(!object.isNull("remain_stat")) {
                        String strRemain = object.getString("remain_stat"); // 마커 색깔 변경 plenty 100개이상 초록, some 30-99 노랑, few 2-29 빨강, empty 0-1 회색
                        String strStock = object.getString("stock_at");
                        String strType = object.getString("type");
                        PharmacyData pharmacyData = new PharmacyData();
                        pharmacyData.setStraddr(straddr);
                        pharmacyData.setStrCode(strCode);
                        pharmacyData.setStrCreate(strCreate);
                        pharmacyData.setdLat(dLat);
                        pharmacyData.setdLng(dLng);
                        pharmacyData.setStrName(strName);
                        pharmacyData.setStrRemain(strRemain);
                        pharmacyData.setStrStock(strStock);
                        pharmacyData.setStrType(strType);
                        arrPharmacyData.add(pharmacyData);
                    }
                }
                Log.d("TAG","in thread arrPharmarcyData size " +": " +arrPharmacyData.size());
            }
            catch (Exception e)
            {
                String ex = e.toString();
                Log.d("TAG","in thread exception " +": " +ex);
            }
        }
        private double getLat( double lat,double radius,double dgree){
            Log.d("Geo", "getlat :  toRadians : " + toRadians(dgree));
            return Math.sin(toRadians(dgree))*radius + lat;
        }
        private double getlng(double lng,double radius,double dgree){
            Log.d("Geo", "getLng :  toRadians : " + toRadians(dgree));
            return Math.cos(toRadians(dgree))*radius + lng;
        }
        private double toRadians(double degree){
            return degree * (Math.PI / 180);
        }

    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon = null;

        public CustomCalloutBalloonAdapter() {
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            Log.d("nameaaa", poiItem.getItemName());
            return null;
        }
    }
    // Alarm 세팅
    private void setAlarm(){
        //현재시간 구하기
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH");
        String getTime = simpleDate.format(mDate);
        int currTime = Integer.parseInt(getTime);
        Calendar calendar = Calendar.getInstance();

        if((currTime>=0) && (currTime<6) ){
            //울리면 안됨 금일 7시로 설정.
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        else if(currTime>20){
            //울리면 안됨 다음날 7시ㅣ로 설정.
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DATE,1);
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        else{
            //현재 시간 +1로 설정
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, currTime+1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }

        Date currentDateTime = calendar.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 hh시 mm 분", Locale.getDefault()).format(currentDateTime);
        //Preference 에 설정한 값 저장
        SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
        editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
        editor.apply();
        diaryNotification(calendar);
    }
    //Alarm 등록
    void diaryNotification(Calendar calendar)
    {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {


            if (alarmManager != null) {

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }
//        else { //Disable Daily Notifications
//            if (PendingIntent.getBroadcast(this, 0, alarmIntent, 0) != null && alarmManager != null) {
//                alarmManager.cancel(pendingIntent);
//                //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
//            }
//            pm.setComponentEnabledSetting(receiver,
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP);
//        }
    }
    //Notice btn event
    private void setNoticeBtnEvent(){
        Button btn_notice_never = findViewById(R.id.btn_notice_never);
        Button btn_notice_close = findViewById(R.id.btn_notice_close);
        btn_notice_never.setOnClickListener(v -> {
            deleteNotices();
        });
        btn_notice_close.setOnClickListener(v ->{
            findViewById(R.id.notice_popup_layout).setVisibility(View.GONE);
        });

    }
    //공지 내용 DB에 저장
    private void savedNotices(){
        ArrayList<Notice> noticeList = new ArrayList<>();
        //공지사항 타이핑해서 add 해주면 됩니다.
        noticeList.add(new Notice(" 1.  앱에서 제공하는 재고 정보는 실제 현장 판매처의 현황과 5분~10분 정도의 차이가 있습니다."));
        noticeList.add(new Notice(" 2.  일부 약국에서 번호표를 배부한 후 판매하는 경우가 있는데 서비스 정보가 번호표 배부 현황을 반영하지 못하고 있습니다."));
        noticeList.add(new Notice(" 3.  공적 마스크 구매 시 식약처 공적 마스크 구매 안내 블로그를 참고해 주세요."));
        noticeList.add(new Notice(" -  식약처 공적 마스크 구매 안내\n"+"http://blog.naver.com/kfdazzang/221839489769"));
        noticeList.add(new Notice(" 4.  공적 마스크 서비스 시 어려운 환경에서도 최선을 다해주시는 약국, 하나로 마트, 우체국 종사자분들께 감사와 응원을 보내드립니다."));
        //DB에 저장하고 팝업을 띄우도록 합니다.
        new NoticeDbAsyncTask(this, noticeList,1).execute();
    }
    private void deleteNotices(){
        //DB의 모든 공지사항을 삭제합니다.
        new NoticeDbAsyncTask(this,null,3).execute();
    }
    public static class NoticeDbAsyncTask  extends AsyncTask<Void, Void, Integer> {//NoticeList DB관련 백그라운드 작업  taskCode별로 백그라운드 작업다르게 처리

        //Prevent leak
        private WeakReference<Activity> weakActivity;
        public ArrayList<Notice> noticeArrayList;
        private int taskCode = 0;//1 노티 리스트 저장 , 2 노티리스트 가져오기, 3 노티 삭제

        public NoticeDbAsyncTask(Activity activity, ArrayList<Notice> noticeArrayList,int taskCode) {
            weakActivity = new WeakReference<>(activity);
            this.noticeArrayList = noticeArrayList;
            this.taskCode = taskCode;

        }

        @Override
        protected Integer doInBackground(Void... params) {
            int noticeListSize = 0;
            switch (taskCode){
                case 1 : {//노티 리스트 받아서 DB에 저장
                    NoticeDataBase db = Room.databaseBuilder( weakActivity.get(),
                            NoticeDataBase.class, "Notices").build();
                    Notice[] users = new Notice[noticeArrayList.size()];
                    for(int i = 0 ; i < noticeArrayList.size() ; i ++){
                        users[i] = noticeArrayList.get(i);
                    }
                    db.noticeDao().insertNotices(users);
                    noticeListSize = noticeArrayList.size();
                    break;
                }
                case 2 : {//DB의 모든 노티리스트 가져오기
                    NoticeDataBase db = Room.databaseBuilder( weakActivity.get(),
                            NoticeDataBase.class, "Notices").build();
                    noticeArrayList = (ArrayList<Notice>) db.noticeDao().getAllNotice();
                    noticeListSize = noticeArrayList.size();
                    break;
                }
                case 3 : {//Db의 모든 노티 삭제
                    NoticeDataBase db = Room.databaseBuilder( weakActivity.get(),
                            NoticeDataBase.class, "Notices").build();
                    db.noticeDao().nukeTable();
                }
            }
            return noticeListSize;
        }

        @Override
        protected void onPostExecute(Integer userArrayListSize) {
            Activity activity = weakActivity.get();
            if(activity != null) {
                switch (taskCode){
                    case 1 :{
                        if( (noticeArrayList != null) && !noticeArrayList.isEmpty()){
                            setNoticeRcylView();
                            setNoticePopupLayout();
                            GlobalApplication.prefs.saveIsNewPreferences("1");
                            break;
                        }
                    }
                    case 2 :{
                        if( (noticeArrayList != null) && !noticeArrayList.isEmpty()){
                            setNoticeRcylView();
                            setNoticePopupLayout();
                            break;
                        }
                    }
                    case 3 :{
                        //팝업을 띄우지 않게 합니다.
                        activity.findViewById(R.id.notice_popup_layout).setVisibility(View.GONE);
                    }
                }
                return;
            }
        }
        private void setNoticeRcylView(){
            Activity activity = weakActivity.get();
            RecyclerView recyclerView = activity.findViewById(R.id.rcyl_notice);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false));
            RcylNoticeAdapter adapter = new RcylNoticeAdapter(noticeArrayList);
            recyclerView.setAdapter(adapter);
        }
        private void setNoticePopupLayout(){
            Activity activity = weakActivity.get();
            ConstraintLayout notice_popup_layout;
            notice_popup_layout = activity.findViewById(R.id.notice_popup_layout);
            notice_popup_layout.setVisibility(View.VISIBLE);
        }
    }
    private void moveToCurrentLocation(){
        GpsTracker gpsTracker = new GpsTracker(this);
        centerLocation.setLongitude(gpsTracker.longitude);
        centerLocation.setLatitude(gpsTracker.latitude);
        cleanData();
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(gpsTracker.latitude,gpsTracker.longitude),false);
        setLayout();
    }
    private void cleanData(){
        mapView.removeAllPOIItems();
        mapView.removeAllCircles();
        arrPharmacyData.clear();
        storeSum = 0;
        nEmptyCount = 0;
        nFewCount = 0;
        nPlentyCount = 0;
        nSomeCount = 0;
    }


}
