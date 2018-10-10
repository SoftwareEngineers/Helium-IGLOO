package helium.com.igloo.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import helium.com.igloo.R;

public class TutorialSlideAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public int[] images = {R.mipmap.one, R.mipmap.two, R.mipmap.three};
    public String[] headings = {"CREATE LECTURES", "LEARN AND INTERACT", "INDEXED SEARCHING"};
    public String[] description = {
            "You can create your own lecture and do discussions among users live.",
            "Ask questions during the discussion by a video chat or by typing it.",
            "Search topics into videos and easily access them through time stamps. "
    };

    public TutorialSlideAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView tutorialImage = (ImageView) view.findViewById(R.id.tutorial_image);
        TextView tutorialHeading = (TextView) view.findViewById(R.id.tutorial_heading);
        TextView tutorialDesc = (TextView) view.findViewById(R.id.tutorial_description);

        tutorialImage.setImageResource(images[position]);
        tutorialHeading.setText(headings[position]);
        tutorialDesc.setText(description[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }
}
