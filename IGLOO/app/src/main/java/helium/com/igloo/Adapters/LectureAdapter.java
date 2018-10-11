package helium.com.igloo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.R;
import helium.com.igloo.ViewArchiveActivity;
import helium.com.igloo.ViewLectureActivity;

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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(p.getOwnerId()).child("name").getValue(String.class);
                lectureViewHolder.textOwner.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(p.getPublic() != true){
            lectureViewHolder.imagePrivate.setVisibility(View.VISIBLE);
        }
        lectureViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(p.getLive()){
                    Intent intent = new Intent(context,ViewLectureActivity.class);
                    intent.putExtra("key", p.getId());
                    context.startActivity(intent);
                }
                else{
                    Intent intent = new Intent(context,ViewArchiveActivity.class);
                    intent.putExtra("archiveID", p.getArchive_id());
                    context.startActivity(intent);
                }
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
