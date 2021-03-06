package com.example.skif.testingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class ShowPageCode extends Activity {
    private List<AnonymousInfo> _allInfo;
    private ListView _listView;
    private UUID _phoneId;
    final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_page);
        _listView = (ListView) findViewById(R.id.listView);
        String phoneName = getIntent().getExtras().getString("PhoneName");
        _phoneId = UUID.fromString(getIntent().getExtras().getString("PhoneId"));
        String urlStr = Constants.ServerUrl + "/all_contacts/"+phoneName;

        try {

            Requester ut = new Requester();
            ut.GetAllContacts(urlStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                try{
                String name = _allInfo.get(position).ContactName;
                //String number = _allInfo.get(position).VerySecretInfo.get(0);
                Intent intent = new Intent(ShowPageCode.this, ShowContactActivity.class);
                intent.putExtra("Name", name);
                intent.putExtra("Number", "8-999-555-55-55");
                startActivity(intent);}
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    public void GetContacts() {
    }
    public void ReadLocalContacts(View view)
    {
        try {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            Cursor contacts = getContentResolver().query(uri, null, null, null, null);

            contacts.moveToFirst();
            AllInfo info = new AllInfo();
            do {
                AnonymousInfo contactInfo = new AnonymousInfo();
                contactInfo.PhoneId=_phoneId;
                for (int i = 0; i < contacts.getColumnCount(); i++) {
                    contactInfo.VerySecretInfo.put(contacts.getColumnName(i), contacts.getString(i));
                }
                contactInfo.ContactName = contactInfo.VerySecretInfo.get("display_name");

                info.AllInfos.add(contactInfo);
            } while (contacts.moveToNext());

            contacts.close();
            _allInfo = info.AllInfos;
            if (_allInfo != null) {
                // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
                final ArrayAdapter<AnonymousInfo> adapter;
                adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, _allInfo);
                // Привяжем массив через адаптер к ListView
                _listView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.d("nert", e.getMessage());
        }
    }
    public void SendContactsToServer(View view){
        new RequestPostTask().execute("", "", "");
    }
    public void onClickToGetAll(View view) {
        GetContacts();
        if (_allInfo != null) {
            // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
            final ArrayAdapter<AnonymousInfo> adapter;
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, _allInfo);
            // Привяжем массив через адаптер к ListView
            _listView.setAdapter(adapter);
        }
    }
    class Requester {
        public void GetAllContacts(String url) throws IOException {
            new RequestTask().execute(url, url, url);
        }

        class RequestTask extends AsyncTask<String, Void, String> {

            @Override
            public String doInBackground(String... uri) {
                HttpClient httpclient = new SslHttpsClient(getApplicationContext());
                HttpResponse hResponse;
                String responseString = null;
                try {
                    HttpGet hGet = new HttpGet(uri[0]);
                    hResponse = httpclient.execute(hGet);
                    StatusLine statusLine = hResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        hResponse.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                    } else {
                        hResponse.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (IOException e) {
                    Log.d("nert", e.getMessage());
                }
                return responseString;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.d("123", "Response: " + result);
                SetList(result);
            }
        }
    }
    class RequestPostTask extends AsyncTask<String, String, String> {

        @Override
        public String doInBackground(String... uri) {
            String result;
            try {
                InputStream inputStream;
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost http = new HttpPost(Constants.ServerUrl+ "/contacts/0");
                AllInfo anInfo = new AllInfo();
                anInfo.AllInfos = _allInfo;
                Gson gson = new Gson();
                String anInfoJson = gson.toJson(anInfo);
                StringEntity se = new StringEntity(anInfoJson, HTTP.UTF_8);
                se.setContentType("text/json;charset=UTF-8");
                http.setEntity(se);
                http.setHeader("Accept", "application/json;charset=UTF-8");
                http.setHeader("Content-type", "application/json;charset=UTF-8");

                HttpResponse httpResponse = httpclient.execute(http);
                inputStream = httpResponse.getEntity().getContent();
                result = inputStream.toString();
            } catch (Exception exception) {
                result = "0";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("123", "Response: " + result);
            String msg;
            if(!result.equals("0"))
                msg = "Завершено удачно";
            else
                msg = "Ошибка связи";
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
                        context);

                // set title
                dlgAlert.setTitle("Загрузка на сервер");
                dlgAlert.setMessage(msg);
                dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

        }
    }
    public void SetList(String response) {
        Gson gson = new Gson();
        try {
            AllInfo posts = gson.fromJson(response, AllInfo.class);
            _allInfo = posts.AllInfos;
            if (_allInfo != null) {
                // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
                final ArrayAdapter<AnonymousInfo> adapter;
                adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, _allInfo);
                // Привяжем массив через адаптер к ListView
                _listView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.d("nert", e.getMessage());
        }
    }
    public void goToBack(View view){finish();}
}