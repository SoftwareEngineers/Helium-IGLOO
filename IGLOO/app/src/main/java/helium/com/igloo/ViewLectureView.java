package helium.com.igloo;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;

public class ViewLectureView extends AppCompatActivity {

    private Switch more;
    private ConstraintLayout details;
    private String key;
    private DatabaseReference databaseReference;
    private TextView textLectureTitle;
    private EditText textLectureDescription;
    private EditText textQuestion;
    private TextView textLectureOwner;
    private ImageView imageViewPrivate;
    private Button buttonAsk;
    private Button buttonCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        setContentView(R.layout.activity_view_lecture_view);
        more = (Switch)findViewById(R.id.swi_more);
        textLectureTitle = (TextView)findViewById(R.id.txt_lecture_title);
        textLectureDescription = (EditText)findViewById(R.id.txt_lecture_description);
        textQuestion = (EditText)findViewById(R.id.txt_question);
        textLectureOwner = (TextView)findViewById(R.id.txt_lecture_owner);
        details = (ConstraintLayout)findViewById(R.id.lyout_more_details);
        imageViewPrivate = (ImageView)findViewById(R.id.img_private);
        buttonAsk = (Button)findViewById(R.id.btn_ask);
        buttonCall = (Button)findViewById(R.id.btn_call);
        buttonAsk.setOnClickListener(new Click());
        buttonCall.setOnClickListener(new Click());
        more.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    details.setVisibility(View.VISIBLE);
                else
                    details.setVisibility(View.GONE);
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LectureModel lectureModel = dataSnapshot.child(key).getValue(LectureModel.class);
                textLectureTitle.setText(lectureModel.getTitle());
                textLectureDescription.setText(lectureModel.getDescription());
                textLectureOwner.setText(lectureModel.getOwnerId());
                if(lectureModel.getPublic()){
                    imageViewPrivate.setVisibility(View.GONE);
                }
                else{
                    imageViewPrivate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private class Click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
            String questionId = databaseReference.push().getKey();
            QuestionModel question = new QuestionModel();
            question.setQuestion(textQuestion.getText().toString());
            question.setLecture(key);
            question.setId(questionId);
            question.setTime("8:30");
            question.setOwner_id("getAuth.getuis");
            if(v == buttonAsk)
                question.setIs_call(false);
            else
                question.setIs_call(true);
            databaseReference.child(questionId).setValue(question);
        }
    }
}
