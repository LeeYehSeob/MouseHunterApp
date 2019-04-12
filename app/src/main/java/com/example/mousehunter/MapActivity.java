package com.example.mousehunter;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.acl.Permission;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {


    // 유저 정보

    int user_index = 0;


    //맵 관련 선언 [2-1]
    final Marker[] marker = new Marker[1];
    GoogleMap mMap;
    LinearLayout linearLayout ;
    LinearLayout buttonLayout;
    LinearLayout buttonLayout_;

    //맵 관련 선언 [2-2]
    Animation translateUP;
    Animation translateDOWN;

    double currentLatitude = 0.0;
    double currentLongitude = 0.0;

    String currentPointName = "";
    String currentPointAddress = "";
    int currentPointIndex = 0;
    String currentBluetoothAddress = null;
    String currentConnectedBluetoothAddress = "";
    int currentCount = 0;
    String currentDate ="";

    //
    Fragment currentFragment;

    boolean isPageOpen = false;
    boolean isEdit = false;
    boolean isInfo = false;
    boolean isConnection = false;

    // GPSTracker class
    private GpsInfo gps;
    //출처: https://mainia.tistory.com/1153 [녹두장군 - 상상을 현실로]
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    //출처: https://mainia.tistory.com/1153 [녹두장군 - 상상을 현실로]
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    // 출처: https://mainia.tistory.com/1153 [녹두장군 - 상상을 현실로]


    //버튼
    Button addButton;
    Button addButton_;
    Button modifyButton;
    Button modifyButton_;
    Button removeButton;
    Button removeButton_;
    Button infoButton;
    Button infoButton_;

    InputMethodManager inputMethodManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        user_index = Integer.parseInt(intent.getStringExtra("user_index"));
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*-------------------------------좌표 불러오기(최소실행)----------------------------------*/
        // 주소 1 - PointList.po : 좌표 목록 반환(user_index 필요)
        //String url = "http://172.30.1.18:8080/PointList.po";// 집에서 할떄 와이파이 주소
        //String url = "http://192.168.0.71:8080/PointList.po";// 학원에서 할때 와이파이 주소
        String url = getString(R.string.url)+"Tom/PointList.po";//실제 서버 주소

        ContentValues value = new ContentValues();
        value.put("user_index", user_index);

        GetPointListTask getPointListTask = new GetPointListTask(url, value);
        getPointListTask.execute();
        /*----------------------------------------------------------------------------------------*/


        /**
         * 카메라 초기 위치지정
         * GPS 로 현재 위치값으로 이동 //37.250412, 127.022815 -> 학원 위치
         * 줌 레벨은 15
         */
        gps = new GpsInfo(MapActivity.this);
        if (gps.isGetLocation()) {
            currentLatitude = gps.getLatitude();
            currentLongitude = gps.getLongitude();
        }else {
            // GPS 를 사용할수 없으므로
            currentLatitude = 37.250412;
            currentLongitude = 127.022815;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude, currentLongitude)));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            googleMap.animateCamera(zoom);
            gps.showSettingsAlert();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude, currentLongitude)));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        googleMap.animateCamera(zoom);


        /**
         * 메뉴바 초기 위치 지정을 위한 레이아웃 지정
         * linearLayout 은 상단에 올라왔을때 버튼의 위치를 나타내며,
         * translateDOWN 애니메이션으로 하단에 위치하게 한다.
         * 이떄 애니메이션의 Duration 값을 1로주면 바로 내려간다.
         */
        linearLayout = findViewById(R.id.layout);
        translateDOWN = AnimationUtils.loadAnimation(this,R.anim.translate_down);
        linearLayout.startAnimation(translateDOWN);

        SlidingAnimationListener listener = new SlidingAnimationListener();
        translateDOWN.setAnimationListener(listener);
        translateDOWN.setDuration(1);

        /**
         * 맵을 일정시간 눌렀을때의 리스너
         * marker 배열의 0번 인덱스에 새로운 마커를 추가한다.
         * 이때 기존에 마커가 있으면 그 마커를 지운다.
         * 위/경도는 클릭 지점에서 반환된다.
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (marker[0] != null) {
                    marker[0].remove();
                }
                marker[0] = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true).title("새 덫"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                currentLatitude = latLng.latitude;
                currentLongitude = latLng.longitude;
                if (!isPageOpen) {
                    TextView latitude = findViewById(R.id.latitude);
                    TextView longitude = findViewById(R.id.longitude);
                    latitude.setText(new String(String.valueOf(currentLatitude)));
                    longitude.setText(new String(String.valueOf(currentLongitude)));

                }
                currentPointAddress = "";
                currentPointIndex = 0;
                currentBluetoothAddress = null;
                currentCount = 0;
                currentDate = "";
            }
        });

        /**
         * 마커를 클릭했을때 처리
         * 기존에 지정된 마커를 클릭한 경우와
         * 신규로 생성된 마커를 클릭하는 경우로 나뉜다
         */
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!marker.getTitle().equals("새 덫")) { // 기존에 있던 마커의 경우
                    // 변수를 출력하는 로그 :: 변수들은 marker 의 snippet 요소에 저장되어있다.
                    Log.d("MARKER SNIPPET : ", marker.getSnippet());


                    /**
                     * Snippet 의 변수들을 나누어서 전역변수로 저장시키는 루틴
                     * Snippet 포맷 :
                     * 주소:수원시 권선구
                     *      |index ::4[20:15:09:08:25:53]<6>{2019-03-28}
                     */
                    currentLatitude = marker.getPosition().latitude;
                    currentLongitude = marker.getPosition().longitude;
                    currentPointName = marker.getTitle();
                    currentPointAddress = marker.getSnippet().substring(marker.getSnippet().indexOf(":") + 1, marker.getSnippet().indexOf("\n"));
                    currentPointIndex = Integer.parseInt(marker.getSnippet().substring(marker.getSnippet().indexOf("::") + 2, marker.getSnippet().indexOf("[")));
                    currentBluetoothAddress = marker.getSnippet().substring(marker.getSnippet().indexOf("[") + 1, marker.getSnippet().lastIndexOf("]"));
                    currentCount = Integer.parseInt(marker.getSnippet().substring(marker.getSnippet().lastIndexOf("<") + 1, marker.getSnippet().lastIndexOf(">")));
                    currentDate = marker.getSnippet().substring(marker.getSnippet().lastIndexOf("{") + 1, marker.getSnippet().lastIndexOf("}"));

                    // 현재 인덱스를 출력하는 로그
                    Log.d("CURRENT INDEX ::", new String(String.valueOf(currentPointIndex)));
                    if (!isPageOpen) {
                        /**
                         * 공통 텍스트뷰 생성
                         * index, latitude, longitude 의 경우
                         * 모든 프래그먼트가 공통적으로 가지고 있다.
                         */
                        TextView index = findViewById(R.id.index);
                        TextView latitude = findViewById(R.id.latitude);
                        TextView longitude = findViewById(R.id.longitude);

                        if (isEdit) {
                            /**
                             *  등록,수정 프래그먼트의 경우
                             *  좌표의 이름과 주소가 EditText 이므로 바꾸어줘야한다.
                             */
                            Log.d("isEdit : ", new String(String.valueOf(isEdit)));
                            EditText pointName = findViewById(R.id.name);
                            EditText pointAddress = findViewById(R.id.address);
                            pointName.setText(currentPointName);
                            pointAddress.setText(currentPointAddress);
                        } else {

                            /**
                             *  정보조회와 제거 프래그먼트의 경우
                             *  좌표의 이름과 주소가 TextView 이다.
                             *
                             */
                            Log.d("isEdit : ", new String(String.valueOf(isEdit)));
                            Log.d("isInfo : ", new String(String.valueOf(isInfo)));
                            TextView pointName = findViewById(R.id.name);
                            TextView pointAddress = findViewById(R.id.address);
                            TextView bluetoothAddress = findViewById(R.id.bluetooth);

                            bluetoothAddress.setText(currentBluetoothAddress);
                            pointName.setText(currentPointName);
                            pointAddress.setText(currentPointAddress);

                            if (isInfo) {
                                /**
                                 * 정보조회 프래그먼트의 경우
                                 * 포획 수와 마지막 리셋날짜를 지정해줌
                                 */
                                Log.d("isEdit-- : ", new String(String.valueOf(isEdit)));
                                Log.d("isInfo-- : ", new String(String.valueOf(isInfo)));
                                TextView countView = findViewById(R.id.count);
                                TextView resetDate = findViewById(R.id.date);
                                Log.d("Current Count : ",currentCount+"");
                                countView.setText(new String(String.valueOf(currentCount)));
                                resetDate.setText(currentDate);
                            }
                        }

                        /**
                         *  공통속성 텍스트 변경
                         */
                        latitude.setText(new String(String.valueOf(currentLatitude)));
                        longitude.setText(new String(String.valueOf(currentLongitude)));
                        index.setText(new String(String.valueOf(currentPointIndex)));

                    }
                }else {
                    /**
                     * 새 덫일 경우
                     * 전체적인 흐름은 같지만,
                     * 초기화 해주는 값이 다름 -> Snippet 요소가 없기 때문
                     * 새 덫을 초기화할때 포맷을 지정해줘도 될듯
                     */
                    currentLatitude = marker.getPosition().latitude;
                    currentLongitude = marker.getPosition().longitude;
                    currentPointName = marker.getTitle();
                    currentPointAddress = "";
                    currentPointIndex = 0;
                    currentBluetoothAddress = null;
                    currentCount = 0;
                    currentDate = "";

                    Log.d("CURRENT INDEX ::", new String(String.valueOf(currentPointIndex)));
                    if (!isPageOpen) {
                        TextView index = findViewById(R.id.index);
                        TextView latitude = findViewById(R.id.latitude);
                        TextView longitude = findViewById(R.id.longitude);

                        if (isEdit) {
                            Log.d("isEdit : ", new String(String.valueOf(isEdit)));
                            EditText pointName = findViewById(R.id.name);
                            EditText pointAddress = findViewById(R.id.address);
                            pointName.setText(currentPointName);
                            pointAddress.setText(currentPointAddress);
                        } else {
                            Log.d("isEdit : ", new String(String.valueOf(isEdit)));
                            Log.d("isInfo : ", new String(String.valueOf(isInfo)));
                            TextView pointName = findViewById(R.id.name);
                            TextView pointAddress = findViewById(R.id.address);
                            TextView bluetoothAddress = findViewById(R.id.bluetooth);

                            bluetoothAddress.setText(currentBluetoothAddress);
                            pointName.setText(currentPointName);
                            pointAddress.setText(currentPointAddress);

                            if (isInfo) {
                                Log.d("isEdit-- : ", new String(String.valueOf(isEdit)));
                                Log.d("isInfo-- : ", new String(String.valueOf(isInfo)));
                                TextView countView = findViewById(R.id.count);
                                TextView resetDate = findViewById(R.id.date);
                                Log.d("Current Count : ", currentCount + "");
                                countView.setText(new String(String.valueOf(currentCount)));
                                resetDate.setText(currentDate);
                            }
                        }


                        latitude.setText(new String(String.valueOf(currentLatitude)));
                        longitude.setText(new String(String.valueOf(currentLongitude)));
                        index.setText("");

                    }
                }
                return false;
            }
        });

        /**
         *  현재 위치 반환 루틴
         *  checkLocation() 함수는 현재 위치에
         *  마커를 생성해준다.
         *  이 마커는 전역변수 marker[] 에 저장된다.
         */
        Button checkLocation = findViewById(R.id.gpsCheck);
        Button checkLocation_ = findViewById(R.id.gpsCheck_);
        checkLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocation();

            }
        });
        checkLocation_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocation();

            }
        });


        /**
         *  메뉴를 위한 버튼 지정
         *  각 버튼은 프래그먼트를 생성하고
         *  플래그 값을 바꾼다.
         */


        linearLayout = findViewById(R.id.layout);
        translateUP = AnimationUtils.loadAnimation(this,R.anim.translate_up);
        translateDOWN = AnimationUtils.loadAnimation(this,R.anim.translate_down);

        translateDOWN.setAnimationListener(listener);
        translateUP.setAnimationListener(listener);

        buttonLayout = findViewById(R.id.buttonLayout);
        buttonLayout_ = findViewById(R.id.buttonLayout_);




        addButton = findViewById(R.id.ADD);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","ADD");
                isEdit =true;
                isInfo = false;
                currentFragment = new AddFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
            }
        });
        modifyButton = findViewById(R.id.MODIFY);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","MODIFY");
                isEdit =true;
                isInfo = false;
                currentFragment = new ModifyFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
            }
        });
        removeButton = findViewById(R.id.REMOVE);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","REMOVE");
                isEdit =false;
                isInfo = false;
                currentFragment = new RemoveFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
            }
        });
        infoButton = findViewById(R.id.INFO);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","INFO");
                isEdit =false;
                isInfo = false;
                currentFragment = new InfoFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
            }
        });






        addButton_ = findViewById(R.id.ADD_);
        modifyButton_ = findViewById(R.id.MODIFY_);
        removeButton_ = findViewById(R.id.REMOVE_);
        infoButton_ = findViewById(R.id.INFO_);
        addButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","ADD_");
                isEdit =true;
                isInfo = false;
                currentFragment = new AddFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
                translateUP();


            }
        });
        modifyButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","MODIFY_");
                isEdit =true;
                isInfo = false;
                currentFragment = new ModifyFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
                translateUP();

            }
        });
        removeButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","REMOVE_");
                isEdit =false;
                isInfo = false;
                currentFragment = new RemoveFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
                translateUP();

            }
        });
        infoButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","INFO_");
                isEdit =false;
                isInfo = true;
                currentFragment = new InfoFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,currentFragment).commit();
                translateUP();
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("CALL BUTTON :: ","MAP");
                if (marker[0] != null) {
                    marker[0].remove();
                }
                if(!isPageOpen){
                    translateDOWN();
                    hideKeyboard();
                    getSupportFragmentManager().beginTransaction().detach(currentFragment).commit();
                }
            }
        });

    }




    public double cameraMoveDistance (double zoomLevel){
        return zoomLevel/(88.5724 * (zoomLevel*zoomLevel) - 15498.33579);
    }


    // 메뉴 애니메이션
    public void translateDOWN (){

        linearLayout.startAnimation(translateDOWN);
        linearLayout.setClickable(false);

        buttonLayout_.setVisibility(View.VISIBLE);
        buttonLayout_.setEnabled(true);
        addButton_.setEnabled(true);
        modifyButton_.setEnabled(true);
        removeButton_.setEnabled(true);
        infoButton_.setEnabled(true);

        buttonLayout.setEnabled(false);
        addButton.setEnabled(false);
        modifyButton.setEnabled(false);
        removeButton.setEnabled(false);
        infoButton.setEnabled(false);

        if(currentLatitude!=0.0 && currentLongitude!=0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude,currentLongitude)));
    }
    public void translateUP() {

        linearLayout.startAnimation(translateUP);
        linearLayout.setClickable(true);
        buttonLayout_.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);


        buttonLayout_.setEnabled(false);
        addButton_.setEnabled(false);
        modifyButton_.setEnabled(false);
        removeButton_.setEnabled(false);
        infoButton_.setEnabled(false);

        buttonLayout.setEnabled(true);
        addButton.setEnabled(true);
        modifyButton.setEnabled(true);
        removeButton.setEnabled(true);
        infoButton.setEnabled(true);



        double  moveDistance = cameraMoveDistance(mMap.getCameraPosition().zoom);
        if(currentLatitude!=0.0 && currentLongitude!=0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude-moveDistance,currentLongitude)));
    }

    // 애니메이션 리스너
    class  SlidingAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(isPageOpen){
                isPageOpen=false;
                Log.d("isPageOpen : ",""+isPageOpen);

            }else {
                isPageOpen=true;
                Log.d("isPageOpen : ",""+isPageOpen);

                buttonLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


    // GPS 현재 위치 반환 함수
    public void checkLocation(){
        if (!isPermission) {
            callPermission();
            return;
        }
        gps = new GpsInfo(MapActivity.this);

        if (gps.isGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if (marker[0] != null) {
                marker[0].remove();
            }
            marker[0] = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(true).title("새 덫"));
            currentLatitude = latitude;
            currentLongitude = longitude;
            Log.d("check Latlng :::", currentLatitude + " , " +currentLongitude);


            double moveDistance = cameraMoveDistance(mMap.getCameraPosition().zoom);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(isPageOpen ? currentLatitude : currentLatitude - moveDistance, currentLongitude)));
            Log.d("ZoomLevel : ", mMap.getCameraPosition() + "");
            Log.d("moveDistance", moveDistance + "");
        }else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
        //출처: https://mainia.tistory.com/1153 [녹두장군 - 상상을 현실로]
    }



    // 웹 연결 Task
    private class GetPointListTask extends AsyncTask<Void,Void,String> {

        private String url;
        private ContentValues values;

        public  GetPointListTask(String url, ContentValues values){
            this.url = url;
            this.values = values;
        }


        @Override
        protected String doInBackground(Void... voids) {
            String result;
            URLConnection urlConnection = new URLConnection();
            result = urlConnection.request(url,values);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                Log.d("RESULT : ", s);
                JSONObject obj = new JSONObject(s);

                JSONArray arr = obj.getJSONArray("item");

                for(int i =0; i<arr.length();i++){
                    JSONObject temp = arr.getJSONObject(i);

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(temp.getDouble("point_latitude"),temp.getDouble("point_longitude")))
                            .title(temp.getString("point_name"))
                            .snippet(" 주소: " +temp.getString("point_address")+
                                    "\n | index ::"+ temp.getInt("point_index")+
                                    "["+temp.getString("bluetooth_address")+"]"+
                                    "<"+temp.getInt("hunting_count")+">"+
                                    "{"+temp.getString("reset_date")+"}"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    }
    private void hideKeyboard(){
        if (isEdit) {
            EditText pointName = findViewById(R.id.name);
            EditText pointAddress = findViewById(R.id.address);
            inputMethodManager.hideSoftInputFromWindow(pointAddress.getWindowToken(),0);
            inputMethodManager.hideSoftInputFromWindow(pointName.getWindowToken(),0);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    //출처: https://mainia.tistory.com/1153 [녹두장군 - 상상을 현실로]



    // 뒤로가기 버튼 클릭 처리
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료")
                .setMessage("종료하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



}
