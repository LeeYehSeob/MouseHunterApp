package com.example.mousehunter;

import android.content.ContentValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class URLConnection {

    public String request(String _url, ContentValues _params){
        HttpURLConnection urlConn = null;
        StringBuffer sbParams = new StringBuffer();

        if(_params == null)
            sbParams.append("");
        else{
            boolean isAnd = false;

            String key;
            String value;

            for(Map.Entry<String,Object> parameter : _params.valueSet()){
                key = parameter.getKey();
                value = parameter.getValue().toString();

                if(isAnd)
                    sbParams.append("&");

                sbParams.append(key).append("=").append(value);

                if(!isAnd)
                    if(_params.size()>=2)
                        isAnd = true;
            }
        }

        try{
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();


            //[2-1] urlConn 설정
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Accept-Charset","UTF-8");
            urlConn.setRequestProperty("Context_Type","application/x-www-form-urlencoded;cahrset=UTF-8");

            String strParams = sbParams.toString();
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes("UTF-8")); // 출력 스트림에 출력.
            os.flush(); // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행.
            os.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.
            // [2-3]. 연결 요청 확인.
            // 실패 시 null을 리턴하고 메서드를 종료.
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            // [2-4]. 읽어온 결과물 리턴.
            // 요청한 URL의 출력물을 BufferedReader로 받는다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            // 출력물의 라인과 그 합에 대한 변수.
            String line;
            String page = "";

            // 라인을 받아와 합친다.
            while ((line = reader.readLine()) != null){
                page += line;

            }

            return page;
        } catch (MalformedURLException e) { // for URL.
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();

        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }
        return null;

    }
}
