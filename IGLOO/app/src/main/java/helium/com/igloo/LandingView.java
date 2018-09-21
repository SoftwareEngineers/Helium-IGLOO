package helium.com.igloo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LandingView extends AppCompatActivity {

    private Button mGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_view);

        mGetStarted = (Button)findViewById(R.id.get_started);
        mGetStarted.setOnClickListener(new Click());
    }

    private class Click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LandingView.this, TutorialView.class);
            startActivity(intent);
        }
    }
}
