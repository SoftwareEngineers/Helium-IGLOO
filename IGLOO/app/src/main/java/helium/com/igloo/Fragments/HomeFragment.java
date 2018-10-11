package helium.com.igloo.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        super.onCreate(savedInstanceState);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.home_view_pager);

        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.home_tab);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);

        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_newspaper);
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_users);

        return v;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new ArchiveLectureFragment());
        adapter.addFragment(new LiveLectureFragment());

        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }


        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

    }
}
