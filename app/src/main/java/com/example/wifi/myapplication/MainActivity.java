package com.example.wifi.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText phone;
    private EditText password;
    private EditText key;
    private ImageView imgview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button login_btn;
        final Button send_btn;
        Button signup_btn;
        Button getimg_btn;
        final Button search_btn;
        final String[] url = {""};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final StringBuilder[] cookie = {new StringBuilder()};
        textView = findViewById(R.id.textView);
        imgview = findViewById(R.id.imageView);

        final Handler handler=new Handler();
        search_btn = findViewById(R.id.search_btn);



        signup_btn = findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                phone = findViewById(R.id.phone);
                password = findViewById(R.id.password);
                key = findViewById(R.id.key);
                final String phone_et = phone.getText().toString();
                final String password_et = password.getText().toString();
                final String key_et = key.getText().toString();

                Map<String, String> data = new HashMap<>();
                data.put("user", phone_et);
                data.put("password", password_et);
                data.put("key", key_et);
                new Requestthread("signup",data){
                    @Override
                    public void run(){
                        request();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(getResult());
                            }
                        });
                    }
                }.start();
            }
        });


        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                phone = findViewById(R.id.phone);
                final String phone_et = phone.getText().toString();
                Map<String, String> data = new HashMap<>();
                data.put("user", phone_et);
                final Requestthread myreq = new Requestthread("signup", data);
                send_btn.setText(String.valueOf(10));
                send_btn.setEnabled(false);
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                final int[] num = {Integer.parseInt(send_btn.getText().toString())};
                                while (num[0] !=1) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            num[0] = Integer.parseInt(send_btn.getText().toString());
                                            send_btn.setText(String.valueOf(num[0] - 1));
                                        }
                                    });
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        send_btn.setEnabled(true);
                                        send_btn.setText("重新发送验证码");
                                    }
                                });
                            }
                        }
                ).start();


                myreq.start();
                try {
                    myreq.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textView.setText(myreq.getResult());
            }
        });

        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //----------------------- 请求参数
                phone = findViewById(R.id.phone);
                password = findViewById(R.id.password);
                final String phone_et = phone.getText().toString();
                final String password_et = password.getText().toString();

                Map<String, String> data = new HashMap<>();
                data.put("user", phone_et);
                data.put("password", password_et);
                new Requestthread("login", data){
                    @Override
                    public void run(){
                        request();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(getResult());
                            }
                        });
                        cookie[0]=getCookie();
                    }
                }.start();
            }
        });

        getimg_btn = findViewById(R.id.getimg_btn);
        getimg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),url[0],Toast.LENGTH_LONG).show();
                System.out.println(url[0]);
                new Requestthread(url[0],new HashMap<String, String>()) {
                    @Override
                    public void run() {
                        this.request();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imgview.setImageBitmap(getBresult());
                            }
                        });
                    }
                }.start();
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                phone = findViewById(R.id.phone);
                password = findViewById(R.id.password);
                final String phone_et = phone.getText().toString();
                final String password_et = password.getText().toString();

                Map<String, String> data = new HashMap<>();

                if (!phone_et.equals(""))
                    data.put("id",phone_et);
                if (!password_et.equals(""))
                    data.put("searchname",password_et);


                new Requestthread("movie", data) {
                    @Override
                    public void run() {
                        this.setCookie(cookie[0]);

                        this.request();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (getResult() != null &&!getResult().equals("没有登陆")) {

                                    JSONArray json = getJsonresult();
                                    StringBuilder outsb = new StringBuilder();

                                    for (int i = 0; i < json.length(); i++) {
                                        try {
                                            JSONObject jsonobj = json.getJSONObject(i);
                                            if (!phone_et.equals(""))
                                                url[0] =jsonobj.getString("海报");
                                            for (Iterator<String> it = jsonobj.keys(); it.hasNext(); ) {
                                                String s = it.next();
                                                outsb.append(s).append(" ").append(jsonobj.getString(s)).append('\n');
                                            }
                                            outsb.append("\n");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    textView.setText(outsb.toString());
                                } else
                                    textView.setText("没有登陆");
                            }
                        });
                    }
                }.start();

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}