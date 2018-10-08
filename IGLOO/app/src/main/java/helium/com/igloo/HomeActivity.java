package helium.com.igloo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Fragments.ArchiveLectureFragment;
import helium.com.igloo.Fragments.LiveLectureFragment;

public class HomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Button buttonCreate;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        buttonCreate = (Button)findViewById(R.id.btn_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.choose_lecture_type_layout, null);
                Button buttonPublicLecture = (Button)mView.findViewById(R.id.btn_public_lecture);
                buttonPublicLecture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIntent = new Intent(HomeActivity.this, CreatePublicLectureActivity.class);
                        startActivity(mIntent);
                    }
                });
                Button buttonPrivateLecture = (Button)mView.findViewById(R.id.btn_private_lecture);
                buttonPrivateLecture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIntent = new Intent(HomeActivity.this, CreatePrivateLectureActivity.class);
                        startActivity(mIntent);
                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LiveLectureFragment(), "Live Lectures");
        adapter.addFragment(new ArchiveLectureFragment(), "Archive Lectures");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
