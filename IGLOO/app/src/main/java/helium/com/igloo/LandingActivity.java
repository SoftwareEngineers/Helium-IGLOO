package helium.com.igloo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
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

import helium.com.igloo.Models.SubscriptionModel;
import helium.com.igloo.Models.UserModel;

public class LandingActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private List<SubscriptionModel> subscriptionModelList;
    private SubscriptionModel subscriptionModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        auth = FirebaseAuth.getInstance();
        subscriptionModelList = new ArrayList<>();
        Thread timer = new Thread(){

            public void run(){

                try{
                    sleep(3000);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
                finally{
                    if(check()){
                        loadPreferences();
                    }
                    else{
                        startActivity(new Intent(LandingActivity.this, SigningInActivity.class));
                        finish();
                    }

                }
            }
        };
        timer.start();
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

}
