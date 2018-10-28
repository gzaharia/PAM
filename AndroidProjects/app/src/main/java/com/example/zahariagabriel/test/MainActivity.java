package com.example.zahariagabriel.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {



    String url;
    Document doc;
    Bitmap captchaImage;
    Handler handler;
    String []  string=new String[10];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());
        url ="https://www.moldcell.md/sendsms";
        GetRequest();
        ((Button)findViewById(R.id.sendButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                string[0] =((EditText)findViewById(R.id.receiverID)).getText().toString();
                string[1] =((EditText)findViewById(R.id.senderID)).getText().toString();
                string[2] =((EditText)findViewById(R.id.messageID)).getText().toString();
                string[3] = doc.select(".captcha > input[name=captcha_sid]").val();
                string[4] = doc.select(".captcha > input[name=captcha_token]").val();
                string[5] = ((EditText)findViewById(R.id.captchaResponse)).getText().toString();
                string[6] = "1";
                string[7] =  "";
                string[8] = doc.select(".websms-form > input[name=form_build_id]").val();
                string[9] = "websms_main_form";
                PostRequest();
            }
        });
    }
    private void GetRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Connection connection = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").validateTLSCertificates(false);
                    doc = connection.get();
                    System.out.println("qqq" + doc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Element captcha = doc.select(".captcha > img").first();
                if (captcha == null){
                    throw new RuntimeException("eror");
                }
                Connection.Response response = null;
                try{
                    response = Jsoup
                                .connect(captcha.absUrl("src"))
                                .validateTLSCertificates(false)
                                .ignoreContentType(true)
                                .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                captchaImage = BitmapFactory.decodeStream(new ByteArrayInputStream(response.bodyAsBytes()));

                //view image
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(captchaImage);
                    }
                });



            }
        }).start();
    }
    private void PostRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response resp = Jsoup.connect(url)
                            .data("phone", string[0])
                            .data("name", string[1])
                            .data("message" ,string[2])
                            .data("captcha_sid" ,string[3])
                            .data("captcha_token" ,string[4])
                            .data("captcha_response" ,string[5])
                            .data("conditions" ,string[6])
                            .data("op" ,string[7])
                            .data("form_build_id" ,string[8])
                            .data("form_id" ,string[9])
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                            .referrer(url)
                            .followRedirects(true)
                            .validateTLSCertificates(false)
                            .method(Connection.Method.POST)
                            .execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        GetRequest();
                    }
                });


            }
        }).start();
    }
}

