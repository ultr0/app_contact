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
import android.os.Build;

import android.support.v4.app.Fragment;
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
import android.widget.Toast;

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
//    public Context context;

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
     String ip;
     String room;
     String department;
     String post;
     int id;
     String hash;
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
//            try {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(paramss));
                writer.flush();
                writer.close();
//            }
//            finally {
                os.close();
//            }
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
                a[i].ip = jsonArray.getJSONObject(i).getString("ip");
                a[i].room = jsonArray.getJSONObject(i).getString("room");
                a[i].department = jsonArray.getJSONObject(i).getString("department");
                a[i].post = jsonArray.getJSONObject(i).getString("post");
                a[i].id = jsonArray.getJSONObject(i).getInt("id");
                a[i].hash = jsonArray.getJSONObject(i).getString("hash");
                a[i].group = jsonArray.getJSONObject(i).getString("group");
            }


            for (int i = 0; i < jsonArray.length(); i++) {

                String md5hash = "";

                if (a[i].hash.equals("")){
                    md5hash = md5Generate.main(
                            a[i].title+a[i].phone+a[i].ip+a[i].room+a[i].department+a[i].post
                    );
                    Log.d("MD5", md5hash);
                }

                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "ru.pnu.sync")
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, context.getText(R.string.account_name))
                        .build());
//                добавляем ФИО
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                                a[i].surname)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                a[i].name)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                                a[i].patronymic).build());
//                добавляем телефон основной
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                a[i].phone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                Phone.TYPE_WORK).build());
//                добавляем телефон SIP
                String manufacturer = Build.MANUFACTURER;
                if (manufacturer.equals("LENOVO") || manufacturer.equals("LG")) {
                    ops.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(
                                    ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    a[i].ip)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    CommonDataKinds.Phone.TYPE_CUSTOM)
                            .withValue(CommonDataKinds.Phone.LABEL,
                                    "SIP").build());
                }
                else {
                    ops.add(ContentProviderOperation
                            .newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(
                                    ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.SipAddress.SIP_ADDRESS,
                                    a[i].ip)
                            .withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE,
                                    ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK)
                            .build());
                }

//                добавляем имя компании
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.Organization.COMPANY,
                                a[i].group)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
                                ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                        .withValue(
                                CommonDataKinds.Organization.TITLE,
                                a[i].post+", "+a[i].department)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
                                ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                        // это не про компанию, а наглое использование не показываемых опций
                        .withValue(
                                ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
                                a[i].id)
                        .withValue(
                                CommonDataKinds.Organization.JOB_DESCRIPTION,
                                md5hash).build());

                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                                a[i].room)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                                CommonDataKinds.StructuredPostal.TYPE_CUSTOM)
                        .withValue(CommonDataKinds.StructuredPostal.LABEL,
                                "Аудитория")
                        .build());

                try {
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("add contacts", "Контакт: " + a[i].surname + " добавлен");
            }

            Toast.makeText(context,
                    context.getText(R.string.update),
                    Toast.LENGTH_SHORT)
                    .show();

            a = null;
            jsonArray = null;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    }
}
