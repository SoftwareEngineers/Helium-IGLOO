package helium.com.igloo;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import helium.com.igloo.Adapters.TutorialSlideAdapter;

public class TutorialView extends AppCompatActivity {

    private ViewPager mTutorialPager;
    private LinearLayout mTutorialDots;
    private Button mBackButton;
    private Button mNextButton;

    private TextView[] mDots;
    private int mCurrentPage;

    private TutorialSlideAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_view);

        mTutorialPager = (ViewPager) findViewById(R.id.tutorial_pager);
        mTutorialDots = (LinearLayout) findViewById(R.id.tutorial_dots);
        mBackButton = (Button) findViewById(R.id.prev_tutorial_button);
        mNextButton = (Button) findViewById(R.id.next_tutorial_button);

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

            if(position == 0){
                mBackButton.setEnabled(false);
                mNextButton.setEnabled(true);
                mBackButton.setVisibility(View.INVISIBLE);

                mNextButton.setText("NEXT");
                mBackButton.setText("");
            }
            else if(position == mDots.length - 1){
                mBackButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);

                mNextButton.setText("LET'S GO");
                mBackButton.setText("BACK");
            }
            else{
                mBackButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);

                mNextButton.setText("NEXT");
                mBackButton.setText("BACK");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
