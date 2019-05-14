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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class InfoFragment extends Fragment {
    MapActivity activity;
    TextView latitude;
    TextView longitude;
    ViewGroup rootView;
    TextView index;
    TextView pointName;
    TextView pointAddress;
    TextView bluetooth;
    TextView totalCountView;
    TextView resetDate;
    TextView inputCount;
    Button connectionButton;

    boolean isConnection = false;


    //블루 투스 선언
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private BluetoothSocket mBluetoothSocket = null;
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
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("Fragment :::" , "[ INFO ]");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_info_form,container,false);
        latitude = rootView.findViewById(R.id.latitude);
        longitude = rootView.findViewById(R.id.longitude);
        pointName = rootView.findViewById(R.id.name);
        pointAddress = rootView.findViewById(R.id.address);
        index = rootView.findViewById(R.id.index);
        bluetooth = rootView.findViewById(R.id.bluetooth);
        totalCountView = rootView.findViewById(R.id.count);
        resetDate = rootView.findViewById(R.id.date);


        latitude.setText(new String(String.valueOf(activity.currentLatitude)));
        longitude.setText(new String(String.valueOf(activity.currentLongitude)));
        pointName.setText(activity.currentPointName);
        pointAddress.setText(activity.currentPointAddress);
        index.setText(new String(String.valueOf(activity.currentPointIndex)));
        totalCountView.setText(new String(String.valueOf(activity.currentCount)));
        resetDate.setText(activity.currentDate);
        if(activity.currentBluetoothAddress != null){
            bluetooth.setText(activity.currentBluetoothAddress);
        }

        // 블루투스 처리



        connectionButton = rootView.findViewById(R.id.connection);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALL BUTTON :: ","connection_info");

                if(!isConnection) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        showErrorDialog("This device is not implement Bluetooth.");
                    }

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
                    } else {
                        Log.d(TAG, "Initialisation successful.");

                        showPairedDevicesListDialog();
                    }
                    connectionButton.setText("연결해제");
                }else{
                    mConnectedTask.closeSocket();
                    isConnection = false;
                    connectionButton.setText("연결");
                }
            }
        });

        Button countSend = rootView.findViewById(R.id.countSend);
        countSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                Log.d("Date ::",format.format(date));
                Log.d("ResetDate ::",resetDate.getText().toString());
                if(resetDate.getText().toString().length() >0){
                    if(format.format(date).equals(resetDate.getText().toString())){
                        Toast.makeText(activity,"오늘은 등록할수 없습니다!",Toast.LENGTH_SHORT).show();
                    }else{
                        if(inputCount.getText() !=null){
                            Log.d("Count Sending ::", "start");
                            String url = getString(R.string.url)+"AddCount.po";//실제 서버 주소

                            Log.d("hunting count :",inputCount.getText().toString().substring(inputCount.getText().toString().indexOf("::")+2));
                            ContentValues values = new ContentValues();
                            values.put("point_index",index.getText().toString());
                            values.put("hunting_count",inputCount.getText().toString().substring(inputCount.getText().toString().indexOf("::")+2));
                            CountTask countTask = new CountTask(url,values);
                            countTask.execute();
                        }

                    }
                }else{
                    if(inputCount.getText() !=null){
                        Log.d("Count Sending ::", "start");
                        String url = getString(R.string.url)+"AddCount.po";//실제 서버 주소

                        Log.d("hunting count :",inputCount.getText().toString().substring(inputCount.getText().toString().indexOf("::")+2));
                        ContentValues values = new ContentValues();
                        values.put("point_index",index.getText().toString());
                        values.put("hunting_count",inputCount.getText().toString().substring(inputCount.getText().toString().indexOf("::")+2));
                        CountTask countTask = new CountTask(url,values);
                        countTask.execute();
                    }
                }
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
        bluetooth = rootView.findViewById(R.id.bluetooth);
        totalCountView = rootView.findViewById(R.id.count);
        resetDate = rootView.findViewById(R.id.date);


        latitude.setText(new String(String.valueOf(activity.currentLatitude)));
        longitude.setText(new String(String.valueOf(activity.currentLongitude)));
        pointName.setText(activity.currentPointName);
        pointAddress.setText(activity.currentPointAddress);
        index.setText(new String(String.valueOf(activity.currentPointIndex)));
        totalCountView.setText(new String(String.valueOf(activity.currentCount)));
        resetDate.setText(activity.currentDate);
        if(activity.currentBluetoothAddress != null){
            bluetooth.setText(activity.currentBluetoothAddress);
        }
    }

    public class CountTask extends AsyncTask<Void,Void,String>{
        private String url;
        private ContentValues values;

        public CountTask(String url, ContentValues values){
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            URLConnection urlConnection = new URLConnection();
            result = urlConnection.request(url,values);
            Log.d("Count Sending ::", "[]");
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            Log.d("result :: ", s);
            Log.d("Count Sending ::", "end");
            Toast.makeText(getContext(),"전송되었습니다!",Toast.LENGTH_SHORT).show();
            inputCount.setText("");
        }
    }

    // 블루투스 연결 Task
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {


        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "create socket for " + mConnectedDeviceName);

            } catch (IOException e) {
                Log.e(TAG, "socket create failed " + e.getMessage());
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

    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e );
            }

            Log.d( TAG, "connected to "+mConnectedDeviceName);
            Toast.makeText(getContext(),"연결 성공 :: "+mConnectedDeviceName,Toast.LENGTH_SHORT).show();
            isConnection = true;

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
        protected void onProgressUpdate(String... recvMessage) {
            String str = recvMessage[0];
            String countMsg ="";
            String count="";

            if(str!=null) {
                if(str.length()>2){
                    countMsg = str.substring(str.lastIndexOf("\n") + 1, str.lastIndexOf("\n") + str.length());
                }else{
                    countMsg = str.substring(str.lastIndexOf("\n") + 1, str.lastIndexOf("\n") + 2);
                }



                Log.d("input :::", "size of : [" + countMsg + "]");
                count = String.valueOf(Integer.parseInt(countMsg) / 4);

            }else{
                count="null";
            }
            Log.d("Count : ", count + "");
            inputCount = rootView.findViewById(R.id.inputcount);
            inputCount.setText("count ::" +count);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {


                closeSocket();
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

    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    public void showPairedDevicesListDialog() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "No devices have been paired.\n"
                    +"You must pair it with another device.");
            return;
        }

        for (int i=0;i<pairedDevices.length;i++) {
            if(pairedDevices[i].getAddress().equals(bluetooth.getText().toString())){
                Log.d("address :::",pairedDevices[i].getAddress());
                ConnectTask task = new ConnectTask(pairedDevices[i]);
                task.execute();
            }

        }
    }

    public void showErrorDialog(String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
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

    public void showQuitDialog(String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
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
    @Override
    public void onDestroy() {
        super.onDestroy();

        if ( mConnectedTask != null ) {

            mConnectedTask.cancel(true);
        }
    }

}
