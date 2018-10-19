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

import java.util.List;

import helium.com.igloo.Models.LectureModel;

public class LectureRateActivity extends AppCompatActivity {

    private TextView mTitleText;
    private TextView mDescriptionText;
    private EditText mRatingText;
    private LectureModel mLecture;
    private boolean isRated;
    private FirebaseAuth auth;
    private double mTotalRatings;
    private double mLecturerRating;
    private double mNumberofRatings;

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
        loadLecturerRatingDetails();

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

    private void loadLecturerRatingDetails(){
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference profilereference = userReference.child(auth.getCurrentUser().getUid());

        profilereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTotalRatings = dataSnapshot.child("total_ratings").getValue(Double.class);
                mLecturerRating = dataSnapshot.child("rating").getValue(Double.class);
                mNumberofRatings = dataSnapshot.child("number_of_ratings").getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void displayLectureInformation(){
        mTitleText.setText(mLecture.getTitle());
        mDescriptionText.setText(mLecture.getDescription());
    }


    public void ExecuteRateLecture(View view) {
        try{
            double rating = Double.parseDouble(mRatingText.getText().toString());
            mLecturerRating = getAverageRating(rating);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

            userRef.child("rating").setValue(mLecturerRating);
            userRef.child("total_rating").setValue(mTotalRatings);
            userRef.child("number_of_ratings").setValue(mNumberofRatings);

            isRated = true;
            Toast.makeText(getApplicationContext(),String.valueOf(mLecturerRating),Toast.LENGTH_LONG).show();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Please input a number!",Toast.LENGTH_LONG).show();
        }
    }

    private double getAverageRating(double rating){
        mTotalRatings += rating;
        mNumberofRatings++;
        double final_rating = mTotalRatings/mNumberofRatings;

        return final_rating;
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
