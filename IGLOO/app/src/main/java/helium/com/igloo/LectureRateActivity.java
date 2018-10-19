package helium.com.igloo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import helium.com.igloo.Models.LectureModel;

public class LectureRateActivity extends AppCompatActivity {

    private CheckBox mCheckBoxOutstanding;
    private CheckBox mCheckBoxVeryGood;
    private CheckBox mCheckBoxAverage;
    private CheckBox mCheckBoxNotGood;
    private CheckBox mCheckBoxTerrible;
    private LectureModel mLecture;
    private boolean isRated;
    private FirebaseAuth auth;
    private double mTotalRatings;
    private double mLecturerRating;
    private double mNumberofRatings;
    private double mRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_lecture);

        mCheckBoxOutstanding = findViewById(R.id.rating_outstanding);
        mCheckBoxVeryGood = findViewById(R.id.rating_verygood);
        mCheckBoxAverage = findViewById(R.id.rating_average);
        mCheckBoxNotGood = findViewById(R.id.rating_notgood);
        mCheckBoxTerrible = findViewById(R.id.rating_terrible);
        mRating = 0;
        Intent intent = getIntent();

        String mKey = intent.getStringExtra("key");
        mLecture = loadLecture(mKey);
        loadLecturerRatingDetails();

        isRated = false;

        auth = FirebaseAuth.getInstance();



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



    public void ExecuteRateLecture(View view) {
        try{
            if (mRating != 0) {
                mLecturerRating = getAverageRating(mRating);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

                userRef.child("rating").setValue(mLecturerRating);
                userRef.child("total_rating").setValue(mTotalRatings);
                userRef.child("number_of_ratings").setValue(mNumberofRatings);

                isRated = true;
                Toast.makeText(getApplicationContext(), String.valueOf(mLecturerRating), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(),"Please rate first!",Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
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


    public void CheckOutsdanding(View view) {
       mRating = 5;
       mCheckBoxOutstanding.setChecked(true);
       mCheckBoxVeryGood.setChecked(false);
       mCheckBoxAverage.setChecked(false);
       mCheckBoxNotGood.setChecked(false);
       mCheckBoxTerrible.setChecked(false);

    }

    public void CheckVeryGood(View view) {
        mRating = 4;
        mCheckBoxOutstanding.setChecked(false);
        mCheckBoxVeryGood.setChecked(true);
        mCheckBoxAverage.setChecked(false);
        mCheckBoxNotGood.setChecked(false);
        mCheckBoxTerrible.setChecked(false);

    }

    public void CheckAverage(View view) {
        mRating = 3;
        mCheckBoxOutstanding.setChecked(false);
        mCheckBoxVeryGood.setChecked(false);
        mCheckBoxAverage.setChecked(true);
        mCheckBoxNotGood.setChecked(false);
        mCheckBoxTerrible.setChecked(false);

    }

    public void CheckNotGood(View view) {
        mRating = 2;
        mCheckBoxOutstanding.setChecked(false);
        mCheckBoxVeryGood.setChecked(false);
        mCheckBoxAverage.setChecked(false);
        mCheckBoxNotGood.setChecked(true);
        mCheckBoxTerrible.setChecked(false);

    }

    public void CheckTerrible(View view) {
        mRating = 1;
        mCheckBoxOutstanding.setChecked(false);
        mCheckBoxVeryGood.setChecked(false);
        mCheckBoxAverage.setChecked(false);
        mCheckBoxNotGood.setChecked(false);
        mCheckBoxTerrible.setChecked(true);

    }
}
