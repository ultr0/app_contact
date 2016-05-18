package ru.pnu.sync;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.ContactsContract;
import android.content.ContentValues;
import android.content.ContentUris;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;

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

import static android.provider.ContactsContract.*;


/**
 * Created by ultr0 on 29.04.2016.
 */

public class Parse {
    public Context context;

    String http = "http://10.10.15.4:8000";

    Parse(){

    }
    boolean Parse_contacts(List<NameValuePair> card, Context context, String profile){
        ///Log.d("LOLOLO", URLEncodedUtils.format(card, "utf-8") );
        new Parse_contacts(context,0).execute("");
        return true;
    }

class contact{
     String title;
     String name;
     String surname;
     String patronymic;
     String phone;
     int ip;
     String room;
     String department;
     String post;
     String group;
}

class Parse_contacts extends AsyncTask<String, Void, String> {

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultJson = "";
    Context context;
    int url_a;
    public Parse_contacts(Context con, int url) {
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
            urlConnection.setRequestMethod("GET");
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

        Log.d("users", strJson);

        JSONObject dataJsonObj = null;
        JSONArray jsonArray = null;


        try {
            jsonArray = new JSONArray(strJson);

            contact[] a = new contact[jsonArray.length()];
            Log.d("length json", String.valueOf(jsonArray.length()));
//            var.edu_rasp_api[] a = new var.edu_rasp_api[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {

                a[i] = new contact();
                Log.d("json", jsonArray.getJSONObject(i).getString("title")
                        + " " + jsonArray.getJSONObject(i).getString("name")
                        + " " + jsonArray.getJSONObject(i).getString("surname")
                        + " " + jsonArray.getJSONObject(i).getString("patronymic")
                        + " " + jsonArray.getJSONObject(i).getString("phone")
                        + " " + jsonArray.getJSONObject(i).getString("ip")
                        + " " + jsonArray.getJSONObject(i).getString("room")
                        + " " + jsonArray.getJSONObject(i).getString("department")
                        + " " + jsonArray.getJSONObject(i).getString("post")
                        + " " + jsonArray.getJSONObject(i).getString("group"));
                a[i].title = jsonArray.getJSONObject(i).getString("title");
                a[i].name = jsonArray.getJSONObject(i).getString("name");
                a[i].surname = jsonArray.getJSONObject(i).getString("surname");
                a[i].patronymic = jsonArray.getJSONObject(i).getString("patronymic");
                a[i].phone = jsonArray.getJSONObject(i).getString("phone");
                a[i].ip = jsonArray.getJSONObject(i).getInt("ip");
                a[i].room = jsonArray.getJSONObject(i).getString("room");
                a[i].department = jsonArray.getJSONObject(i).getString("department");
                a[i].post = jsonArray.getJSONObject(i).getString("post");
                a[i].group = jsonArray.getJSONObject(i).getString("group");
            }


            for (int i = 0; i < jsonArray.length(); i++) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                a[i].surname).build());
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                a[i].phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                Phone.TYPE_MOBILE).build());
                try {
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("add contacts", "Контакт: " + a[i].surname + " добавлен");
            }


//            Systemf i_kill_you = new Systemf();
//            var.edu_rasp_api[][] debilism;
//            debilism = i_kill_you.get_struct_weekday_rasp(a);
//            for(int i=0; i<6; i++){
//
//                Log.d("struct","----------day-"+(i+1)+"----------- len:"+debilism[i].length);
//                for(var.edu_rasp_api strong:debilism[i]){
//                    try{
//                        Log.d("struct",strong.discipline+" "+strong.type_p);}
//                    catch(Exception e){
//
//                    }
//                }
//            }

//            dialog.dismiss();
//            ((main)context).displayView(0,debilism);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    }
}
