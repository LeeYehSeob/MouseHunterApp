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
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {
    boolean isIDChecked = false;
    InputMethodManager inputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        ConstraintLayout layout = findViewById(R.id.signUpLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });

        Button checkID = findViewById(R.id.checkButton);
        checkID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText user_id = findViewById(R.id.signUpId);
                String id = user_id.getText().toString();
                String url = getString(R.string.url)+"IDCheck.user";
                ContentValues idValues = new ContentValues();
                idValues.put("id",id);
                if(!Pattern.matches("^[a-zA-Z0-9]{6,15}$",id)){
                    Toast.makeText(getApplicationContext(),"아이디 형식이 올바르지 않습니다!",Toast.LENGTH_SHORT).show();
                    return;
                }
                IDCheckTask idCheckTask = new IDCheckTask(url,idValues);
                idCheckTask.execute();
            }
        });


        //확인 버튼
        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isIDChecked){
                    Toast.makeText(getApplicationContext(),"중복체크를 먼저 해주세요!",Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText user_id = findViewById(R.id.signUpId);
                EditText user_pw = findViewById(R.id.signUpPass);
                EditText user_name = findViewById(R.id.signUpName);
                EditText user_email = findViewById(R.id.signUpEmail);
                EditText user_phone1 = findViewById(R.id.phone1);
                EditText user_phone2 = findViewById(R.id.phone2);
                EditText user_phone3 = findViewById(R.id.phone3);
                RadioGroup radioGroup = findViewById(R.id.genderRadio);
                RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());

                String id = user_id.getText().toString();
                String pw = user_pw.getText().toString();
                String name = user_name.getText().toString();
                String email = user_email.getText().toString();
                String phone1 = user_phone1.getText().toString();
                String phone2 = user_phone2.getText().toString();
                String phone3 = user_phone3.getText().toString();
                String gender;




                /**********************************  유효성 처리  *********************************/
                if(!Pattern.matches("^[a-zA-Z0-9]{6,15}$",id)){
                    Toast.makeText(getApplicationContext(),"아이디 형식이 올바르지 않습니다!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pw.length() < 8){
                    Toast.makeText(getApplicationContext(),"비밀번호가 너무 짧습니다!!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!Pattern.matches("^[a-zA-Z0-9!@#$%^*+=-_]{8,20}$" , pw)){
                    Toast.makeText(getApplicationContext(),"비밀번호 형식이 올바르지 않습니다!",Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    String ragex = "^["+id+"]+$";
                    if(Pattern.matches(ragex,pw)){
                        Toast.makeText(getApplicationContext(),"비밀번호에 아이디가 포함되어 있습니다!",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(Pattern.matches("^[0-9]{8,20}$",pw) || Pattern.matches("^[a-zA-Z]{8,20}$",pw)){
                        Toast.makeText(getApplicationContext(),"숫자와 영어가 1글자 이상 포함되어야 합니다!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(Pattern.matches("^[!@#$%^*+=-_]{8,20}$",pw)){
                        Toast.makeText(getApplicationContext(),"비밀번호는 특수문자로만 구성될 수 없습니다!",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(),"이메일 형식이 올바르지 않습니다!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!Pattern.matches("^010|011|016|017|018|019$",phone1)||!Pattern.matches("^[0-9]{3,4}$",phone2)||!Pattern.matches("^[0-9]{4}$",phone3)){
                    Toast.makeText(getApplicationContext(),"휴대폰번호 형식이 올바르지 않습니다!",Toast.LENGTH_SHORT).show();
                    return;
                }


                if(radioButton !=null) {
                    gender = radioButton.getText().toString();
                    Log.d("gender :",gender);
                }else{
                    Toast.makeText(getApplicationContext(),"성별을 선택해 주세요!!",Toast.LENGTH_SHORT).show();
                    return;
                }


                PasswordEncryption passwordEncryption = new PasswordEncryption(pw,SignUpActivity.this);
                Log.d("pass : ",passwordEncryption.getPass());
                String url = getString(R.string.url)+"SignUp.user";//실제 서버 주소
                ContentValues signUpValue = new ContentValues();
                signUpValue.put("user_id",id);
                signUpValue.put("user_pw", passwordEncryption.getPass());
                signUpValue.put("user_name", name);
                signUpValue.put("user_email",email);
                signUpValue.put("phone1",phone1);
                signUpValue.put("phone2",phone2);
                signUpValue.put("phone3",phone3);
                signUpValue.put("user_gender",gender);
                signUpValue.put("fromWeb","false");
                SignUpTask signUpTask = new SignUpTask(url,signUpValue);
                signUpTask.execute();



            }
        });




        //취소버튼
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 액티비티로
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    public class SignUpTask extends AsyncTask<Void,Void,String>{

            private String url;
            private ContentValues values;

            public  SignUpTask(String url, ContentValues values){
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
                if(s.equals("true")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setTitle("회원가입 완료").setMessage("가입이 완료되었습니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setTitle("가입실패").setMessage("회원가입에 실패하였습니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                super.onPostExecute(s);
                Log.d("string result ---------",s);
                //알림창 띄우고 로그인 화면으로

            }
    }
    public class IDCheckTask extends AsyncTask<Void,Void,String>{

        private String url;
        private ContentValues values;

        public  IDCheckTask(String url, ContentValues values){
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
            Log.d("result :: ", s);
            if(s.equals("true")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                builder.setTitle("중복 검사").setMessage("사용 가능한 아이디 입니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isIDChecked = true;
                        return;
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                builder.setTitle("중복 검사").setMessage("사용할수 없는 아이디 입니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText user_id = findViewById(R.id.signUpId);
                        user_id.setText("");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            super.onPostExecute(s);
            Log.d("string result ---------",s);
            //알림창 띄우고 로그인 화면으로

        }
    }
    private void hideKeyboard(){
        EditText user_id = findViewById(R.id.signUpId);
        EditText user_pw = findViewById(R.id.signUpPass);
        EditText user_name = findViewById(R.id.signUpName);
        EditText user_email = findViewById(R.id.signUpEmail);
        EditText user_phone1 = findViewById(R.id.phone1);
        EditText user_phone2 = findViewById(R.id.phone2);
        EditText user_phone3 = findViewById(R.id.phone3);

        inputMethodManager.hideSoftInputFromWindow(user_id.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_pw.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_name.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_email.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_phone1.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_phone2.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(user_phone3.getWindowToken(),0);

    }

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
