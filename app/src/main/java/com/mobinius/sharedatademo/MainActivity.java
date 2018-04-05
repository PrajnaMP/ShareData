package com.mobinius.sharedatademo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button mChoose,mSend;
    private ImageView mContainer;
    private static final int FILE_SELECT_CODE = 0;
    private Context context = MainActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChoose = (Button) findViewById(R.id.choose_button);
        mSend = (Button) findViewById(R.id.send_button);
        mContainer = (ImageView) findViewById(R.id.image_view);
        mChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });


        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                }
                Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   //Get all declared methods in WifiManager class
                boolean methodFound=false;
                for(Method method: wmMethods){
                    if(method.getName().equals("setWifiApEnabled")){
                        methodFound=true;
                        WifiConfiguration netConfig = new WifiConfiguration();
                        netConfig.SSID = "\""+"TinyBox"+"\"";
                        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

                        try {
                            boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);
                            for (Method isWifiApEnabledmethod: wmMethods)
                            {
                                if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                                    while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
                                    };
                                    for(Method method1: wmMethods){
                                        if(method1.getName().equals("getWifiApState")){
                                            int apstate;
                                            apstate=(Integer)method1.invoke(wifiManager);
                                        }
                                    }
                                }
                            }
                            if(apstatus)
                            {
                                System.out.println("SUCCESSdddd");
//                                               method();


                            }else
                            {
                                System.out.println("FAILED");

                            }

                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }





            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.image_view);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.v("tag", "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = FileUtils.getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.v("tag", "File Path: " + path);

                    Bitmap bmp = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/Screenshots/Screenshot_2017-07-25-12-01-35.png");
                    mContainer.setImageBitmap(bmp);

                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
*/


   private void method(){


       WifiConfiguration conf = new WifiConfiguration();
       conf.SSID = "\"\"" + "TinyBox" + "\"\"";
       conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
       WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
       wifiManager.addNetwork(conf);

       List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
       for( WifiConfiguration i : list ) {
           if(i.SSID != null && i.SSID.equals("\"\"" + "TinyBox" + "\"\"")) {
               try {
                   wifiManager.disconnect();
                   wifiManager.enableNetwork(i.networkId, true);
                   System.out.print("i.networkId " + i.networkId + "\n");
                   wifiManager.reconnect();
                   break;
               }
               catch (Exception e) {
                   e.printStackTrace();
               }

           }
       }
   }





}

