package ru.pnu.sync;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ru.pnu.common.logger.Log;

/**
 * Created by ultr0 on 29.04.2016.
 */


private class Parse extends AsyncTask<String, Void, String> {

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    Context context;
    int url_a;
    public Parse(Context con, int url) {
        super();
        this.url_a = url;
        context = con;
    }

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            dialog = ProgressDialog.show(context, "","Загрузка рассписания", true);
//            dialog.show();
//       }

    @Override
    protected String doInBackground(String... params) {
        // получаем данные с внешнего ресурса
        try {

            URL url = new URL("http://10.10.15.4:8000/portal_api/m/contacts/");
            Log.e("connect", "10.10.15.4:8000/portal_api/m/contacts/");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            List<NameValuePair> paramss = new ArrayList<NameValuePair>();
            paramss.add(new BasicNameValuePair("token", "ed6071b2e238ac5147afd56125e2ba0f"));


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(paramss));
            writer.flush();
            writer.close();
            os.close();
            int responseCode=urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    resultJson+=line;
                }
            }
            else {
                resultJson="";

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);
        // выводим целиком полученную json-строку

        Log.d("Luser", strJson);

        JSONObject dataJsonObj = null;
        String secondName = "";

        String title = "";
        String description = "";
        String completed = "";
        JSONArray jsonArray = null;



        try {
            jsonArray = new JSONArray(strJson);


            var.edu_rasp_api[] a = new var.edu_rasp_api[jsonArray.length()];

            for (int i=0; i<jsonArray.length(); i++){

                a[i] = new var.edu_rasp_api();
                Log.d("json", jsonArray.getJSONObject(i).getString("discipline") + " " + jsonArray.getJSONObject(i).getString("room")+" "+jsonArray.getJSONObject(i).getString("teacher")+" "+jsonArray.getJSONObject(i).getString("weekday"));
                a[i].discipline=jsonArray.getJSONObject(i).getString("discipline");
                a[i].weekday=jsonArray.getJSONObject(i).getInt("weekday");
                a[i].room=jsonArray.getJSONObject(i).getString("room");
                a[i].subgroup=jsonArray.getJSONObject(i).getString("s_br");
                a[i].para=jsonArray.getJSONObject(i).getInt("hour");
                a[i].week_type=jsonArray.getJSONObject(i).getString("w_t");
                a[i].type_p=jsonArray.getJSONObject(i).getString("type");
                a[i].teacher=jsonArray.getJSONObject(i).getString("teacher");
            }

            Systemf i_kill_you = new Systemf();
            var.edu_rasp_api[][] debilism;
            debilism = i_kill_you.get_struct_weekday_rasp(a);
            for(int i=0; i<6; i++){

                Log.d("struct","----------day-"+(i+1)+"----------- len:"+debilism[i].length);
                for(var.edu_rasp_api strong:debilism[i]){
                    try{
                        Log.d("struct",strong.discipline+" "+strong.type_p);}
                    catch(Exception e){

                    }
                }
            }

            dialog.dismiss();
            ((main)context).displayView(0,debilism);
        } catch (JSONException e) {

        }
        catch (Exception e){
            e.printStackTrace();
        }



    }
}