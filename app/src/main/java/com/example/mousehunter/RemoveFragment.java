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
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoveFragment extends Fragment {
    MapActivity activity;
    TextView latitude;
    TextView longitude;
    ViewGroup rootView;
    TextView index;
    TextView pointName;
    TextView pointAddress;

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
        Log.d("Fragment :::" , "[ REMOVE ]");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_remove_form,container,false);
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
                String url = getString(R.string.url)+"PointDelete.po";//실제 서버 주소
                ContentValues values = new ContentValues();
                values.put("point_index",index.getText().toString());
                DeleteTask pointTask = new DeleteTask(url,values);
                pointTask.execute();


                activity.mMap.clear();
                url = getString(R.string.url)+"PointList.po";
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
        Button submit = rootView.findViewById(R.id.submit);
    }

    public class DeleteTask extends AsyncTask<Void,Void,String> {
        private String url;
        private ContentValues values;

        public  DeleteTask(String url, ContentValues values){
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
    }
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
