package helium.com.igloo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.R;
import helium.com.igloo.RegisterView;
import helium.com.igloo.ViewLectureView;

public class LectureAdapter extends RecyclerView.Adapter<LectureAdapter.LectureViewHolder> {
    private List<LectureModel> lectures;
    private Context context;

    public LectureAdapter(List<LectureModel> lectures, Context context) {
        this.lectures = lectures;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    @Override
    public void onBindViewHolder(final LectureViewHolder lectureViewHolder, int i) {
        final LectureModel p = lectures.get(i);
        lectureViewHolder.textOwner.setText(p.getOwnerId());
        lectureViewHolder.textTitle.setText(p.getTitle());
        lectureViewHolder.texttime_created.setText(p.getTime_created());
        if(p.getPublic() != true){
            lectureViewHolder.imagePrivate.setVisibility(View.VISIBLE);
        }
        lectureViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ViewLectureView.class);
                intent.putExtra("key", p.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
public LectureAdapter.LectureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.lecture_layout, viewGroup, false);
        return new LectureViewHolder(itemView);
    }


    public class LectureViewHolder extends RecyclerView.ViewHolder{
        protected TextView textOwner;
        protected TextView texttime_created;
        protected TextView textTitle;
        protected ImageView imageView;
        protected ImageView imagePrivate;
        protected View view;

        public LectureViewHolder(View v) {
            super(v);
            view = v;
            textOwner = (TextView) v.findViewById(R.id.txt_lecture_owner);
            textTitle = (TextView) v.findViewById(R.id.txt_lecture_title);
            texttime_created = (TextView) v.findViewById(R.id.txt_time_created);
            imageView  = (ImageView)v.findViewById(R.id.imageView);
            imagePrivate = (ImageView)v.findViewById(R.id.img_private);
        }
    }

}
