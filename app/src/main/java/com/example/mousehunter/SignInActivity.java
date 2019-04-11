package com.example.mousehunter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignInActivity extends AppCompatActivity {
    InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Button toSignUpForm = findViewById(R.id.signUpFormButton);
        toSignUpForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ConstraintLayout layout = findViewById(R.id.signInLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });

        Button signInButton = findViewById(R.id.SignInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //로그인 정보 입력 받기
                EditText user_id = findViewById(R.id.signInId);
                EditText user_pw = findViewById(R.id.signInPass);
                String id = user_id.getText().toString();
                String pw = user_pw.getText().toString();

                //url 지정
                //String url = "http://172.30.1.18:8080/SignIn.user";// 집에서 할떄 와이파이 주소
                //String url = "http://192.168.0.71:8080/SignIn.user";// 학원에서 할때 와이파이 주소
                String url = "http://iotmit.iptime.org:83/Tom/SignIn.user";//실제 서버 주소

                //서버에 전달할 ContentValues 객체 생성 및 초기화

                PasswordEncryption passwordEncryption = new PasswordEncryption(pw);
                ContentValues value = new ContentValues();
                value.put("id",id);
                value.put("pw", passwordEncryption.getPass());

                SignInTask signInTask = new SignInTask(url,value);
                signInTask.execute();

            }
        });

    }
    public class SignInTask extends AsyncTask<Void,Void,String> {

        private String url;
        private ContentValues values;

        public  SignInTask(String url, ContentValues values){
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
            Log.d("msg : ",s);
            String result = s.substring(0,s.indexOf("["));
            if(result.equals("true")){
                String user_index = s.substring(s.indexOf("[")+1,s.indexOf("]"));
                Intent intent = new Intent(getApplicationContext(),MapActivity.class);
                intent.putExtra("user_index", user_index);
                Log.d("user_index ::" , user_index);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(SignInActivity.this,"패스워드가 다릅니다.",Toast.LENGTH_SHORT).show();
            }

            //알림창 띄우고 로그인 화면으로
        }
    }

    private void hideKeyboard(){
        EditText user_id = findViewById(R.id.signInId);
        EditText user_pw = findViewById(R.id.signInPass);

        inputMethodManager.hideSoftInputFromWindow(user_id.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_pw.getWindowToken(),0);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
