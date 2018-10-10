package helium.com.igloo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import helium.com.igloo.Adapters.TutorialSlideAdapter;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager mTutorialPager;
    private LinearLayout mTutorialDots;
    private Button mBackButton;
    private Button mNextButton;
    private Button mSkipButton;
    private Button mGoButton;

    private FirebaseAuth auth;

    private TextView[] mDots;
    private int mCurrentPage;

    private TutorialSlideAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mTutorialPager = (ViewPager) findViewById(R.id.tutorial_pager);
        mTutorialDots = (LinearLayout) findViewById(R.id.tutorial_dots);
        mBackButton = (Button) findViewById(R.id.prev_tutorial_button);
        mNextButton = (Button) findViewById(R.id.next_tutorial_button);
        mSkipButton = (Button) findViewById(R.id.skip_tutorial_button);
        mGoButton = (Button) findViewById(R.id.go_tutorial_button);

        mSkipButton.setOnClickListener(new Click());
        mGoButton.setOnClickListener(new Click());

        sliderAdapter = new TutorialSlideAdapter(this);
        mTutorialPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        mTutorialPager.addOnPageChangeListener(viewListener);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialPager.setCurrentItem(mCurrentPage - 1);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialPager.setCurrentItem(mCurrentPage + 1);
            }
        });
    }

    private class Click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(TutorialActivity.this, HomeActivity.class));
        }
    }

    public void addDotsIndicator(int position){
        mDots = new TextView[3];
        mTutorialDots.removeAllViews();

        for(int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(50);
            mDots[i].setTextColor(getResources().getColor(R.color.blur));

            mTutorialDots.addView(mDots[i]);
        }

        if(mDots.length > 0){
            mDots[position].setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            mCurrentPage = position;

            if(position == mDots.length - 1){
                mBackButton.setEnabled(true);
                mNextButton.setEnabled(false);
                mSkipButton.setEnabled(false);
                mGoButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.INVISIBLE);
                mSkipButton.setVisibility(View.INVISIBLE);
                mGoButton.setVisibility(View.VISIBLE);
            }
            else{
                mBackButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mSkipButton.setEnabled(true);
                mGoButton.setEnabled(false);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.VISIBLE);
                mSkipButton.setVisibility(View.VISIBLE);
                mGoButton.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void onBackPressed(){
        moveTaskToBack(false);
    }
}
