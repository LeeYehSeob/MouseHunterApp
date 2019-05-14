package com.example.mousehunter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class AddFragment extends Fragment {
    MapActivity activity;
    TextView latitude;
    TextView longitude;
    EditText pointName;
    EditText pointAddress;
    ViewGroup rootView;
    TextView index;
    TextView bluetooth;
    ConnectedTask mConnectedTask = null;
    Button connetionButton;


    //블루투스 선언
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";

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
        Log.d("Fragment :::" , "[ ADD ]");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_add_form,container,false);
        latitude = rootView.findViewById(R.id.latitude);
        longitude = rootView.findViewById(R.id.longitude);
        pointName = rootView.findViewById(R.id.name);
        pointAddress = rootView.findViewById(R.id.address);
        index = rootView.findViewById(R.id.index);
        bluetooth = rootView.findViewById(R.id.bluetooth);




        latitude.setText(new String(String.valueOf(activity.currentLatitude)));
        longitude.setText(new String(String.valueOf(activity.currentLongitude)));
        pointName.setText(activity.currentPointName);
        pointAddress.setText(activity.currentPointAddress);
        index.setText(new String(String.valueOf(activity.currentPointIndex)));
        bluetooth.setText(activity.currentConnectedBluetoothAddress);

        LinearLayout layout = rootView.findViewById(R.id.addLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(pointName,pointAddress);
            }
        });

        Button submit = rootView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","submit");
                pointName = rootView.findViewById(R.id.name);
                pointAddress = rootView.findViewById(R.id.address);


                //url 지정
                //String url = "http://172.30.1.18:8080/PointAdd.po";// 집에서 할떄 와이파이 주소
                //String url = "http://192.168.0.71:8080/PointAdd.po";// 학원에서 할때 와이파이 주소
                String url = getString(R.string.url)+"PointAdd.po";//실제 서버 주소
                ContentValues values = new ContentValues();
                values.put("user_index",activity.user_index);
                values.put("point_name",pointName.getText().toString());
                values.put("point_address",pointAddress.getText().toString());
                values.put("point_latitude",activity.currentLatitude);
                values.put("point_longitude",activity.currentLongitude);
                values.put("bluetooth_address", activity.currentConnectedBluetoothAddress);

                PointTask pointTask = new PointTask(url,values);
                pointTask.execute();

                activity.mMap.clear();
                //url = "http://192.168.0.71:8080/PointList.po";
                url = getString(R.string.url)+"PointList.po";
                ContentValues value = new ContentValues();
                value.put("user_index", activity.user_index);
                GetPointListTask getPointListTask = new GetPointListTask(url,value);
                getPointListTask.execute();



            }
        });

        connetionButton = rootView.findViewById(R.id.connection);
        connetionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","connection_add");
                if(!activity.isConnection){
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        showErrorDialog("This device is not implement Bluetooth.");
                        return;
                    }

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
                    }
                    else {
                        Log.d(TAG, "Initialisation successful.");

                        showPairedDevicesListDialog();

                    }
                    connetionButton.setText("연결해제");

                }
                else{
                    mConnectedTask.closeSocket();
                    activity.isConnection = false;
                    connetionButton.setText("연결");
                }


            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public class PointTask extends AsyncTask<Void,Void,String>{
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
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( TAG, "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( TAG, "socket create failed " + e.getMessage());
            }

        }


        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }

                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSucess) {

            if ( isSucess ) {
                connected(mBluetoothSocket);
            }
            else{

                isConnectionError = true;
                Log.d( TAG,  "Unable to connect device");
                showErrorDialog("Unable to connect device");
            }
        }
    }
    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }
    private class ConnectedTask extends AsyncTask<Void, String, Boolean>{
        private BluetoothSocket mBluetoothSocket = null;
        private InputStream mInputStream = null;
        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e );
            }

            Log.d( TAG, "connected to "+mConnectedDeviceName);
            Toast.makeText(getContext(),"연결 성공 :: "+mConnectedDeviceName,Toast.LENGTH_SHORT).show();

            Toast.makeText(getContext(),"연결이 해제되면 주소를 받아옵니다.",Toast.LENGTH_LONG).show();

            activity.isConnection = true;

        }
        @Override
        protected Boolean doInBackground(Void... params) {

            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;


            while (true) {

                if ( isCancelled() ) return false;

                try {

                    int bytesAvailable = mInputStream.available();

                    if(bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];

                        mInputStream.read(packetBytes);

                        for(int i=0;i<bytesAvailable;i++) {

                            byte b = packetBytes[i];
                            if(b == '\n')
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                Log.d(TAG, "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {

                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }

        }
        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {
                closeSocket();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.container,new AddFragment()).commit();


                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }
        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        void closeSocket(){

            try {

                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }
    }

    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.currentConnectedBluetoothAddress = pairedDevices[which].getAddress();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }
    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                }
            }
        });
        builder.create().show();
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
    private void hideKeyboard(EditText point_name,EditText point_address){
        ((MapActivity)getActivity()).inputMethodManager.hideSoftInputFromWindow(point_name.getWindowToken(),0);
        ((MapActivity)getActivity()).inputMethodManager.hideSoftInputFromWindow(point_address.getWindowToken(),0);
    }
}
