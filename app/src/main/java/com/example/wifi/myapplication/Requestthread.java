package com.example.wifi.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Requestthread extends Thread {
    private String result; //请求结果
    private StringBuilder urlbuilder = new StringBuilder(); //构造一个请求链接
    private StringBuilder cookie = new StringBuilder(); //请求,返回的cookie
    private Bitmap bresult; //图片输出


    //构造函数,目的是为了创建对象
    Requestthread(String url, Map<String, String> data) //url:访问路径  Map<String,String> data  是请求参数
    {
        setUrlbuilder(url,data);
    } //构造结束



    public void request() {
        HttpURLConnection connection = null; //定义连接对象
        try {
            URL url = new URL(urlbuilder.toString()); //连接路径
            connection = (HttpURLConnection) url.openConnection(); //创建连接对象
            connection.setRequestMethod("GET");//设置访问方式为“GET”
            connection.setConnectTimeout(10000);//设置连接服务器超时时间为8秒
            connection.setReadTimeout(10000);//设置读取服务器数据超时时间为8秒
            connection.setUseCaches(false);

            if (0 != cookie.length()) //有没有对这个请求设置cookie
                connection.setRequestProperty("Cookie", cookie.toString()); //设置请求头

            connection.connect();

            this.result = String.valueOf(connection.getResponseCode()); //获取http响应
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) { //判断是否请求成功 (HttpURLConnection.HTTP_OK == 200 )
                //从服务器获取响应并把响应数据转为字符串打印
                InputStream in = connection.getInputStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int httplenth = 0;

                byte[] buffer = new byte[1024]; //中间变量 用于单次读取http响应
                int len;
                while ((len = in.read(buffer)) != -1) { //没有读取到文件结尾
                    out.write(buffer, 0, len);
                    httplenth += len; // -1 <1024 =1024
                }

                byte[] data = out.toByteArray(); //把输出流转换成字节数组

                bresult = BitmapFactory.decodeByteArray(data, 0, httplenth);


                out.close();
                in.close();

                Map<String, List<String>> head = connection.getHeaderFields(); //取出响应头
                for (String i : head.keySet()) {
                    if (i != null && i.equals("Set-Cookie"))//取出set-cookie
                    {
                        System.out.println(head.get(i));
                        for (String j : head.get(i)) {
                            cookie.append(j);
                        }
                    }
                }
                result = new String(data, 0, httplenth);

            }
        } catch (Exception e) {
            result = e.toString();
            System.out.println(result);
            e.printStackTrace();
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }

    }



    @Override
    public void run() {
        this.request();
    }

    String getResult() {
        return result;
    }

    void setUrlbuilder(String url, Map<String, String> data) {

        urlbuilder=new StringBuilder();
        if (!url.contains("http")) {//1.判断请求是否是http / https  2.绝对路径(图片)与相对路径(服务器)通用
            urlbuilder.append("http://111.230.150.41:8000/").append(url).append("?"); //动态构造链接
        }else {
            url = url.replaceAll("https", "http"); //针对豆瓣的图片
            urlbuilder.append(url).append("?"); //动态构造链接
        }

        if (data!=null) {
            boolean flag = false; //看是否有参数
            for (String key : data.keySet()) { //遍历参数
                flag = true;
                urlbuilder.append(key);
                urlbuilder.append("=");
                urlbuilder.append(data.get(key)); // {'num'='123456','password'='123'} ==> ''==>num=123456&password=123
                urlbuilder.append("&");
            }
            if (flag)
                urlbuilder.deleteCharAt(urlbuilder.length() - 1); //去掉&元素
        }
    }

    String geturl() {
        return urlbuilder.toString();
    }

    void setCookie(StringBuilder cookie) {
        this.cookie = cookie;
    }

    StringBuilder getCookie() {
        return cookie;
    }


    Bitmap getBresult() {
        return bresult;
    }


    JSONArray getJsonresult() {
        try {
            return new JSONArray(result);
        } catch (JSONException e) {
            try {
                return new JSONArray('[' + result + ']');
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return null;
        }
    }

}