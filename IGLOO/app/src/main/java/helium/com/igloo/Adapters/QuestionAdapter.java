package helium.com.igloo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.R;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionModel> questions;
    private Context context;
    private Drawable drawable;

    public QuestionAdapter(List<QuestionModel> questions, Context context) {
        this.questions = questions;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return questions.size();
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
                final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Lectures");
                databaseReference1.child(p.getId()).child("answered").setValue(true);
            }
        });
    }

    @Override
public QuestionAdapter.QuestionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.question_layout, viewGroup, false);
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
