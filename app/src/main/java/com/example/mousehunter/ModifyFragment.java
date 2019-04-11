package com.example.mousehunter;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModifyFragment extends Fragment {

    MapActivity activity;
    TextView latitude;
    TextView longitude;
    EditText pointName;
    EditText pointAddress;
    ViewGroup rootView;
    TextView index;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = (MapActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Fragment :::" , "[ MODIFY ]");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_modify_form,container,false);

        latitude = rootView.findViewById(R.id.latitude);
        longitude = rootView.findViewById(R.id.longitude);
        pointName = rootView.findViewById(R.id.name);
        pointAddress = rootView.findViewById(R.id.address);
        index = rootView.findViewById(R.id.index);


        latitude.setText(new String(String.valueOf(activity.currentLatitude)));
        longitude.setText(new String(String.valueOf(activity.currentLongitude)));
        pointName.setText(activity.currentPointName);
        pointAddress.setText(activity.currentPointAddress);
        index.setText(new String(String.valueOf(activity.currentPointIndex)));

        Button submit = rootView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","submit");



                //url 지정
                //String url = "http://172.30.1.18:8080/PointModify.po";// 집에서 할떄 와이파이 주소
                //String url = "http://192.168.0.71:8080/PointModify.po";// 학원에서 할때 와이파이 주소
                String url = "http://iotmit.iptime.org:83/Tom/PointModify.po";//실제 서버 주소


                ContentValues values = new ContentValues();
                values.put("point_index",activity.currentPointIndex);
                values.put("point_name",pointName.getText().toString());
                values.put("point_address",pointAddress.getText().toString());
                values.put("point_latitude",activity.currentLatitude);
                values.put("point_longitude",activity.currentLongitude);

                PointTask pointTask = new PointTask(url,values);
                pointTask.execute();


                activity.mMap.clear();
                //url = "http://192.168.0.71:8080/PointList.po";
                url = "http://iotmit.iptime.org:83/Tom/PointList.po";
                ContentValues value = new ContentValues();
                value.put("user_index", activity.user_index);
                GetPointListTask getPointListTask = new GetPointListTask(url,value);
                getPointListTask.execute();

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        latitude = rootView.findViewById(R.id.latitude);
        longitude = rootView.findViewById(R.id.longitude);
        pointName = rootView.findViewById(R.id.name);
        pointAddress = rootView.findViewById(R.id.address);
        index = rootView.findViewById(R.id.index);


        latitude.setText(new String(String.valueOf(activity.currentLatitude)));
        longitude.setText(new String(String.valueOf(activity.currentLongitude)));
        pointName.setText(activity.currentPointName);
        pointAddress.setText(activity.currentPointAddress);
        index.setText(new String(String.valueOf(activity.currentPointIndex)));
    }

    public class PointTask extends AsyncTask<Void,Void,String> {
        private String url;
        private ContentValues values;

        public  PointTask(String url, ContentValues values){
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
            //super.onPostExecute(s);
            Log.d("result :: ", s);
        }
    }private class GetPointListTask extends AsyncTask<Void,Void,String> {

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

                    activity.mMap.addMarker(new MarkerOptions()
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
}
