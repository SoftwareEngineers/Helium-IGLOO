package helium.com.igloo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.UserModel;

public class LectureRateActivity extends AppCompatActivity {

    private TextView mTitleText;
    private TextView mDescriptionText;
    private EditText mRatingText;
    private LectureModel mLecture;
    private boolean isRated;
    private FirebaseAuth auth;
    private UserModel mLecturer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_lecture);

        mTitleText = findViewById(R.id.lecture_title_rate);
        mDescriptionText = findViewById(R.id.lecture_description_rate);
        mRatingText = findViewById(R.id.lecture_input_rate);

        Intent intent = getIntent();

        String mKey = intent.getStringExtra("key");
        mLecture = loadLecture(mKey);
        isRated = false;

        auth = FirebaseAuth.getInstance();

        displayLectureInformation();
    }

    public LectureModel loadLecture(final String key){
        final LectureModel[] lecture = new LectureModel[1];
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lecture[0] = dataSnapshot.child(key).getValue(LectureModel.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return lecture[0];
    }

    public void displayLectureInformation(){
        mTitleText.setText(mLecture.getTitle());
        mDescriptionText.setText(mLecture.getDescription());
    }


    public void ExecuteRateLecture(View view) {
        try{
            double rating = Double.parseDouble(mRatingText.getText().toString());
            mLecturer.setRating(rating);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

            userRef.child("rating").setValue(mLecturer.getRating());
            isRated = true;
            Toast.makeText(getApplicationContext(),String.valueOf(mLecturer.getRating()),Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Please input a number!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed(){
        if(isRated){
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(),"please rate first",Toast.LENGTH_LONG).show();
        }
    }
}
