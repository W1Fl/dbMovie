package com.example.wifi.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private EditText phone;
    private EditText password;
    private EditText key;

    Button login_btn;
    Button send_btn;
    Button signup_btn;
    Button search_btn;


    private void clearview() {
        LinearLayout movies =findViewById(R.id.moviesView);

        movies.removeAllViews();
    }

    private void setview(Bitmap img, JSONObject jsonobj) {


        LinearLayout movies =  findViewById(R.id.moviesView);
        View globalView = View.inflate(this, R.layout.item, null);


        TextView movietext = globalView.findViewById(R.id.movietextView);
        ImageView movieimg = globalView.findViewById(R.id.movieimageView);


        try {
            SpannableString moviename = new SpannableString(jsonobj.getString("片名"));
            RelativeSizeSpan movienamesize = new RelativeSizeSpan(1.5f);
            moviename.setSpan(movienamesize, 0, moviename.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            movietext.append(moviename);
            movietext.append("\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }




        String id = null;

        try {
            for (Iterator<String> it = jsonobj.keys(); it.hasNext(); ) {
                String s = it.next();
                if (s.equals("海报")) {
                    continue;
                }
                if (s.equals("片名")) {
                    continue;
                }
                if (s.equals("id")) {
                    id = jsonobj.getString(s);
                    continue;
                }
                if (s.equals("评分")) {
                    movietext.append("豆瓣评分 " + jsonobj.getString(s) + " 分" + '\n');
                    continue;
                }
                if (s.equals("观看地址")) {

                    movietext.append("观看地址 ");
                    String[] links = jsonobj.getString("观看地址").split("\n");
                    for (String link : links) {
                        final String[] L = link.split("\t");


                        SpannableString linkname = new SpannableString(L[0]);

                        linkname.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.this, L[1], Toast.LENGTH_SHORT).show();

                                Uri uri= Uri.parse(L[1]);
                                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                startActivity(intent);
                            }
                        }, 0, linkname.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#0000FF"));
                        linkname.setSpan(foregroundColorSpan, 0, linkname.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        movietext.append(linkname);
                        movietext.append(" ");
                    }
                    movietext.append("\n");

                    continue;
                }
                movietext.append(s+" "+jsonobj.getString(s)+'\n');
                continue;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        movietext.setMovementMethod(LinkMovementMethod.getInstance());
        movieimg.setImageBitmap(img);


        final String finalId = id;
        movieimg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                phone.setText(finalId);

                search_btn.performClick();

            }
        });

        movies.addView(globalView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final StringBuilder[] cookie = {new StringBuilder()};
        textView = findViewById(R.id.textView);

        final Handler handler = new Handler();
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
                new Requestthread("signup", data) {
                    @Override
                    public void run() {
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
                                while (num[0] != 1) {
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
                new Requestthread("login", data) {
                    @Override
                    public void run() {
                        request();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(getResult());
                            }
                        });
                        cookie[0] = getCookie();
                    }
                }.start();
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                phone = findViewById(R.id.phone);
                password = findViewById(R.id.password);
                key = findViewById(R.id.key);
                final String key_et = key.getText().toString();
                final String phone_et = phone.getText().toString();
                final String password_et = password.getText().toString();

                Map<String, String> data = new HashMap<>();

                if (!phone_et.equals(""))
                    data.put("id", phone_et);
                if (!password_et.equals(""))
                    data.put("searchname", password_et);

                data.put("id_start", "0");
                data.put("id_limit", (key_et.equals("")) ? "1000" : key_et);

                clearview();

                new Requestthread("movie", data) {

                    @Override
                    public void run() {
                        this.setCookie(cookie[0]);

                        this.request();
                        System.out.println(getResult());

                        JSONArray json = getJsonresult();

                        Bitmap img;

                        for (int i = 0; i < json.length(); i++) {
                            try {

                                final JSONObject jsonobj = json.getJSONObject(i);

                                setUrlbuilder(jsonobj.getString("海报"), null);

                                request();

                                img = getBresult();


                                final Bitmap finalImg = img;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setview(finalImg, jsonobj);
                                    }
                                });

                            } catch (JSONException e) {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(getResult());
                                    }
                                });

                                e.printStackTrace();
                            }
                        }
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