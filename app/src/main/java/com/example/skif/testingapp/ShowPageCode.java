package com.example.skif.testingapp;

import android.app.Activity;
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
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by skif on 14.12.2014.
 */
public class ShowPageCode extends Activity {
    private List<AnonymousInfo> _allInfo;
    private ListView _listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_page);
        _listView = (ListView) findViewById(R.id.listView);
        String phoneName = getIntent().getExtras().getString("PhoneName");
        String urlStr = Constants.ServerUrl + "all_contacts/"+phoneName;

        try {

            Requester ut = new Requester();
            ut.GetAllContacts(urlStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                String info = _allInfo.get(position).ContactName;
                Intent intent = new Intent(ShowPageCode.this, ShowPageCode.class);
                intent.putExtra("info", info);
                startActivity(intent);
            }
        });*/
    }
    public void GetContacts() {
        try {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            Cursor contacts = getContentResolver().query(uri, null, null, null, null);

            contacts.moveToFirst();
            AllInfo info = new AllInfo();
            do {
                AnonymousInfo contactInfo = new AnonymousInfo();
                contactInfo.ContactName = contacts.getString(25);
                for (int i = 0; i < contacts.getColumnCount(); i++) {
                    contactInfo.VerySecretInfo.put(contacts.getColumnName(i), contacts.getString(i));
                }

                info.AllInfos.add(contactInfo);
            } while (contacts.moveToNext());

            contacts.close();
            _allInfo = info.AllInfos;
        } catch (Exception e) {
            String dd = e.getMessage();
        }
    }

    public void onClickToGetAll(View view) {
        GetContacts();
        if (_allInfo != null) {
            // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
            final ArrayAdapter<AnonymousInfo> adapter;
            adapter = new ArrayAdapter<AnonymousInfo>(this,
                    android.R.layout.simple_list_item_1, _allInfo);
            // Привяжем массив через адаптер к ListView
            _listView.setAdapter(adapter);
        }
    }
    class Requester {
        private String response;

        public AnonymousInfo Get(String id) throws IOException {
            String urlStr = Constants.ServerUrl+"this/" + id;

            new RequestTask().execute(urlStr, urlStr, urlStr);
            //получаем ответ от сервера
            AnonymousInfo anInfo = new Gson().fromJson(response, AnonymousInfo.class);
            return anInfo;
        }

        public void GetAllContacts(String url) throws IOException {
            new RequestTask().execute(url, url, url);
        }

        public String Post(String info) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost http = new HttpPost(Constants.ServerUrl+"/post");
            //AnonymousInfo anInfo = new AnonymousInfo();
            //anInfo.ContactName = info;
            Gson gson = new Gson();
            String anInfoJson = gson.toJson(_allInfo);

            String response = httpclient.execute(http, new BasicResponseHandler());
            return response;
        }


        class RequestTask extends AsyncTask<String, Void, String> {

            @Override
            public String doInBackground(String... uri) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse hResponse;
                String responseString = null;
                try {
                    HttpGet hGet = new HttpGet(uri[0]);
                    /*hGet.addHeader("User-Agent", "User-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like\n" +
                            "Gecko) Chrome/39.0.2171.71 Safari/537.36");*/
                    hResponse = httpclient.execute(hGet);
                    StatusLine statusLine = hResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        hResponse.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                    } else {
                        //Closes the connection.
                        hResponse.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (ClientProtocolException e) {
                    Log.d("nert", e.getMessage());
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
    public void SetList(String response) {
        Gson gson = new Gson();
        try {
            AllInfo posts = gson.fromJson(response, AllInfo.class);
            _allInfo = posts.AllInfos;
            if (_allInfo != null) {
                // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
                final ArrayAdapter<AnonymousInfo> adapter;
                adapter = new ArrayAdapter<AnonymousInfo>(this,
                        android.R.layout.simple_list_item_1, _allInfo);
                // Привяжем массив через адаптер к ListView
                _listView.setAdapter(adapter);
            }
        } catch (Exception e) {
            String r = e.getMessage();
        }
    }
    public void goToBack(View view){finish();}
}