/*
 * 25 June MMXXIV PUBLIC DOMAIN by JML
 *
 * The authors disclaim copyright to this source code
 *
 */
package com.cod5.droid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cod5.droid.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 189;

    //  static {
    //    System.loadLibrary("signyourapk");
    // }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.root.getSettings().setJavaScriptEnabled(true);

        binding.root.addJavascriptInterface(new JsObject(), "MyObject");
        binding.root.loadUrl("file:///android_res/raw/index.html");
        binding.root.loadData("<html><head><script>\n" +
                "    function bob() {\n" +
                "        document.body.innerText = 'MyObject.toString(8);'\n" +
                "    }\n" +
                "\n" +
                "</script></head><body>k</body></html>", "text/html", null);
        binding.root.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String weburl){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    binding.root.evaluateJavascript("alert('4');", null);
                } else {
                    binding.root.loadUrl("javascript:document.body.innerText=MyObject.toString(7);");
                }
            }
        });


        //binding.sampleText.setText(R.string.sign_you_apk);
        //binding.button.setOnClickListener(this::onClickMe);
        if (hasWriteStoragePermission()) {
            //binding.sampleText.setText(R.string.sign_you_apk);
        }
        askAllFilesPermission();
        listDownloadsFiles();
    }
    static class JsObject {
        @JavascriptInterface
        public String toString(String s) { return "injectedObject" + s; }
    }


    private void listDownloadsFiles() {
        File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] lst = d.listFiles();
        int i = 0;
        if (lst != null) {
            for (File f : lst) {
                if (f.getName().endsWith(".apk")) {
                    RadioButton r;
                    r = new RadioButton(this);
                    r.setText(f.getAbsolutePath());
                    r.setId(i);
                    //binding.radio.addView(r, i);
                    i++;
                }
            }
        }
    }

    /* print result of permission request */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (hasAllFilesPermission()) {
                return;
            }
            startActivity(
                    new Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    )
            );
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private boolean hasAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private boolean hasWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE
                );
                return false;
            }
        }
        return true;

    }

    public void onClickMe(View v) {

        {
            //binding.sampleText.setText(R.string.start);
            Toast.makeText(MainActivity.this, "Start signing", Toast.LENGTH_LONG).show();
        }
        try {
            InputStream in;
            OutputStream out;
            AssetManager assetManager = getAssets();

            {
                int id = 0; // binding.radio.getCheckedRadioButtonId();
                if (id >= 0) {
                    //RadioButton rdb = binding.radio.findViewById(id);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        File cert = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/cert.bks");
                        if (!cert.exists()) {
                            in = assetManager.open("cert.bks");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                out = Files.newOutputStream(cert.toPath());
                            } else {
                                out = new FileOutputStream(cert);
                            }
                            copyFile(in, out);
                            out.close();
                        }
                        //binding.sampleText.setText(R.string.apk_signed_v2);
                    } else {
                        File cert = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/certificate.jks");
                        Log.d("Sign(v1):", cert.getPath() + " " + " ");

                        //binding.sampleText.setText(R.string.apk_signed);
                    }

                    Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_LONG).show();
                } else {
                    //binding.sampleText.setText(R.string.please_select_an_apk);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() != null) {
                //binding.sampleText.setText(e.getCause().toString());
            } else {
                //binding.sampleText.setText(R.string.failed);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            //binding.sampleText.setText(String.format("%s/app.apk", this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        }
    }

    //public native String stringFromJNI();
}