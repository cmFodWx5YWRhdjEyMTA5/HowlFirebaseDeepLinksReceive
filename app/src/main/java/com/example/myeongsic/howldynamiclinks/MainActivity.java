package com.example.myeongsic.howldynamiclinks;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.main_textView);


        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();


                            getTime(deepLink.toString());
                        }


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



    }

    void getTime(String url){

        OkHttpClient okHttpClient = new OkHttpClient();

        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {


                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        textView.setText(jsonObject.getString("time"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            });



            }
        });
    }

    void createDeepLink(){

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.kakao.com"))
                .setDynamicLinkDomain("r37xk.app.goo.gl")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.kakao.talk").build())
                // Open links with com.example.ios on iOS
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();




        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLinkUri)
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            System.out.println(shortLink.toString());
                            //Uri flowchartLink = task.getResult().getPreviewLink();
                        } else {
                            // Error
                            // ...
                        }
                    }
                });
    }
}
