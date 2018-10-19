package helium.com.igloo.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.R;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionModel> questions;
    private Context context;
    private Drawable drawable;
    private VideoView videoView;
    private MediaPlayer mp;

    public QuestionAdapter(List<QuestionModel> questions, Context context, VideoView videoView) {
        this.questions = questions;
        this.context = context;
        this.videoView = videoView;
    }

    public QuestionAdapter(List<QuestionModel> questions, Context context) {
        this.questions = questions;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public void getMediaPlayer(MediaPlayer mediaPlayer){
        this.mp = mediaPlayer;
    }

    @Override
    public void onBindViewHolder(final QuestionViewHolder questionViewHolder, int i) {
        final QuestionModel p = questions.get(i);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(p.getOwner_id()).child("name").getValue(String.class);
                questionViewHolder.textOwner.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(p.getIs_call()){
            questionViewHolder.textOwner.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_settings_phone_black_24dp, 0);
        }
        questionViewHolder.textQuestion.setText(p.getQuestion());
        questionViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Questions");
                if(!p.getIs_answered()) {
                    databaseReference1.child(p.getId()).child("is_answered").setValue(true);
                    DateFormat format = new SimpleDateFormat("hh:mm a MMM-dd-yyyy");
                    try {
                        Date date = format.parse(p.getTime());
                        long milli = new Date().getTime() - date.getTime();
                        databaseReference1.child(p.getId()).child("time_answered").setValue(milli);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    mp.seekTo((int)p.getTime_answered());
                }
            }
        });
    }

    @Override
public QuestionAdapter.QuestionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_question, viewGroup, false);
        return new QuestionViewHolder(itemView);
    }


    public class QuestionViewHolder extends RecyclerView.ViewHolder{
        protected TextView textOwner;
        protected EditText textQuestion;
        protected ImageView imageView;
        protected View view;

        public QuestionViewHolder(View v) {
            super(v);
            view = v;
            textOwner = (TextView) v.findViewById(R.id.txt_question_owner);
            textQuestion = (EditText) v.findViewById(R.id.txt_question);
            imageView  = (ImageView)v.findViewById(R.id.imageView);
        }
    }

}
