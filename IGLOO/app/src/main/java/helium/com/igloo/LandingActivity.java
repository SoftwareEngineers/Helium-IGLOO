package helium.com.igloo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Extras.RequestPermission;
import helium.com.igloo.Models.SubscriptionModel;
import helium.com.igloo.Models.UserModel;

public class LandingActivity extends RequestPermission {

    private FirebaseAuth auth;
    private static final int REQUEST_PERMISSION = 10;
    private boolean granted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        auth = FirebaseAuth.getInstance();
        Thread timer = new Thread(){

            public void run(){

                try{
                    sleep(3000);
                    if(check() && checkPermissions()){
                        loadPreferences();
                    }
                    else {
                        requestAppPermissions(new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_NETWORK_STATE,},
                                R.string.permision_message, REQUEST_PERMISSION);
                    }
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        };
        timer.start();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_LONG).show();
        if(check()){
            loadPreferences();
        }else {
            startActivity(new Intent(LandingActivity.this, SigningInActivity.class));
        }

    }

    private void loadPreferences() {

        SharedPreferences settings = getSharedPreferences("User", Context.MODE_PRIVATE);
        String email = settings.getString("email", null);
        String password = settings.getString("password", null);

        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LandingActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    startActivity(new Intent(LandingActivity.this,HomeActivity.class));
                }
            });
        }
        catch(Exception ie){
            ie.printStackTrace();
        }
    }

    private boolean check(){
        boolean flag = false;

        SharedPreferences settings = getSharedPreferences("User", Context.MODE_PRIVATE);
        String email = settings.getString("email", null);
        String password = settings.getString("password", null);

        if(email != null && password != null) {
            flag = true;
        }
        return flag;
    }
    private boolean checkPermissions(){
        boolean flag = true;
        if (ContextCompat.checkSelfPermission(LandingActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(LandingActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(LandingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(LandingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            flag = false;
        }
        return flag;
    }

}
