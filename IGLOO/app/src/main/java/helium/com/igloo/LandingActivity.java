package helium.com.igloo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LandingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        final Intent i = new Intent(LandingActivity.this, SigningInActivity.class);
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
                finally{
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }
}
