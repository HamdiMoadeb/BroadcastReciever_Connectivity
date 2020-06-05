package com.outsider.broadcastreceiverexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class MainActivity extends AppCompatActivity {

    ImageView imageView, imagePicked;
    Button btnQRCode, btnImage;
    private static final int REQUEST_CODE_QR_SCAN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageview);
        imagePicked = findViewById(R.id.imageviewPicked);
        btnQRCode = findViewById(R.id.qrcodebtn);
        btnImage = findViewById(R.id.imagepickerbtn);


        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImagePicker.Companion.with(MainActivity.this)
                            .crop()
                            .compress(1024)
                            .maxResultSize(1080, 1080)
                            .start();

            }
        });

        btnQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED){
                    Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
                    startActivityForResult( i,REQUEST_CODE_QR_SCAN);
                }else{
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            1);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if(requestCode == REQUEST_CODE_QR_SCAN) {
                if(data==null)
                    return;
                //Getting the passed result
                String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                Log.d("Tagg","Have scan result in your app activity :"+ result);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Scan result");
                alertDialog.setMessage(result);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }else{
                Uri fileUri = data.getData();
                imagePicked.setImageURI(fileUri);
                imagePicked.setVisibility(View.VISIBLE);
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(MainActivity.this, "data.getError", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(112 == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "L'application a été autorisée à écrire sur votre stockage!", Toast.LENGTH_LONG).show();
                // Reload the activity with permission granted or use the features what required the permission
            } else {
                Toast.makeText(this,"L'application n'était pas autorisée à écrire sur votre stockage. Par conséquent, il ne peut pas fonctionner correctement. S'il vous plaît envisager d'accorder cette permission" , Toast.LENGTH_LONG).show();
            }
        }
    }

    BroadcastReceiver broadcastReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                boolean connected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (connected){
                    imageView.setVisibility(View.VISIBLE);
                    imagePicked.setVisibility(View.GONE);
                    btnImage.setVisibility(View.GONE);
                    btnQRCode.setVisibility(View.GONE);

                }else{
                    imageView.setVisibility(View.GONE);
                    imagePicked.setVisibility(View.GONE);
                    btnImage.setVisibility(View.VISIBLE);
                    btnQRCode.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }
}
