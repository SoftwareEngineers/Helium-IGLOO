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
    private boolean isRated;
    private FirebaseAuth auth;
    private double mTotalRatings;
    private double mLecturerRating;
    private double mNumberofRatings;
    private double mRating;
    private String owner_id;
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
        mTotalRatings = 0;
        mNumberofRatings = 0;
        owner_id = getIntent().getExtras().getString("owner_id");
        isRated = false;
        auth = FirebaseAuth.getInstance();
        loadLecturerRatingDetails();


    }

    private void loadLecturerRatingDetails(){
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference profilereference = userReference.child(owner_id);

        profilereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTotalRatings = dataSnapshot.child("totalRatings").getValue(double.class);
                mLecturerRating = dataSnapshot.child("rating").getValue(double.class);
                mNumberofRatings = dataSnapshot.child("numberOfRatings").getValue(int.class);

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
                DatabaseReference userRef = databaseReference.child(owner_id);

                userRef.child("totalRatings").setValue(mLecturerRating);
                userRef.child("rating").setValue(mTotalRatings);
                userRef.child("numberOfRatings").setValue(mNumberofRatings);

                isRated = true;
                Toast.makeText(getApplicationContext(), String.valueOf(mLecturerRating), Toast.LENGTH_LONG).show();
                finish();
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
            super.onBackPressed();

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
