package helium.com.igloo;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Adapters.LectureAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.SubscriptionModel;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton mProfileBack;
    private CircleImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private RatingBar mRating;
    private TextView mSubscribers;
    private TextView mLectures;
    private TextView mTokens;
    private RecyclerView mLecturesList;
    private Button mSubscribe;
    private Button mUnsubscribe;

    private List<LectureModel> lectures;
    private LectureAdapter lectureAdapter;
    private String profileKey;
    private String profileName;
    private double noOfSubscribers;

    private FirebaseAuth auth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Intent intent = getIntent();

        mProfileBack = (ImageButton)findViewById(R.id.profile_back);
        mProfilePic = (CircleImageView) findViewById(R.id.profile_picture);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mRating = (RatingBar)findViewById(R.id.profile_rating);
        mSubscribers = (TextView) findViewById(R.id.profile__subscribers);
        mLectures = (TextView) findViewById(R.id.profile_lectures);
        mTokens = (TextView) findViewById(R.id.profile_tokens);
        mLecturesList = (RecyclerView) findViewById(R.id.profile_lectures_list);
        mSubscribe = (Button) findViewById(R.id.profile_subscribe);
        mUnsubscribe = (Button) findViewById(R.id.profile_unsubscribe);

        if(!intent.getStringExtra("profileID").equals(auth.getCurrentUser().getUid())){
            profileKey = intent.getStringExtra("profileID");
            isSubscribe();
        }
        else {
            profileKey = auth.getCurrentUser().getUid();
        }
        profileName = intent.getStringExtra("profileName");

        lectures = new ArrayList<>();
        lectureAdapter = new LectureAdapter(lectures, this);
        mLecturesList.setAdapter(lectureAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLecturesList.setLayoutManager(layoutManager);

        mRating.setNumStars(5);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(profileKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String url = dataSnapshot.child("profileUrl").getValue(String.class);
                double subscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);
                double lectures = dataSnapshot.child("numberOfLectures").getValue(Double.class);
                double rating = dataSnapshot.child("rating").getValue(Double.class);
                int tokens = dataSnapshot.child("tokens").getValue(Integer.class);

                storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mProfilePic.setImageBitmap(bitmap);
                    }
                });

                mProfileName.setText(name);
                mProfileEmail.setText(email);
                mRating.setRating((float) rating);
                mSubscribers.setText(Integer.toString((int)subscribers));
                mLectures.setText(Integer.toString((int)lectures));
                mTokens.setText(Integer.toString(tokens));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
                final DatabaseReference subscriptionReference = databaseReference.child(auth.getCurrentUser().getUid());

                SubscriptionModel subscription = new SubscriptionModel(profileName, auth.getCurrentUser().getDisplayName(), profileKey,auth.getCurrentUser().getUid(),"pending");
                subscriptionReference.child(profileKey).setValue(subscription);

                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
                final DatabaseReference profileReference = userReference.child(profileKey);

                profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        noOfSubscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);

                        noOfSubscribers++;
                        mSubscribers.setText(Integer.toString((int)noOfSubscribers));
                        profileReference.child("numberOfSubscribers").setValue(noOfSubscribers);
                        mSubscribe.setVisibility(View.INVISIBLE);
                        mUnsubscribe.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
                final DatabaseReference subscriptionReference = databaseReference.child(auth.getCurrentUser().getUid());
                subscriptionReference.child(profileKey).getRef().removeValue();

                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
                final DatabaseReference profileReference = userReference.child(profileKey);

                profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        noOfSubscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);

                        noOfSubscribers--;
                        mSubscribers.setText(Integer.toString((int)noOfSubscribers));
                        profileReference.child("numberOfSubscribers").setValue(noOfSubscribers);
                        mUnsubscribe.setVisibility(View.INVISIBLE);
                        mSubscribe.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        loadLectures();
    }

    private void loadLectures() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");

        databaseReference.orderByChild("time_created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lectures.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("available").getValue(Boolean.class) &&
                            childSnapshot.child("owner_id").getValue(String.class).equals(profileKey)){
                        LectureModel lecture = childSnapshot.getValue(LectureModel.class);

                        lectures.add(lecture);
                        Collections.reverse(lectures);
                        lectureAdapter.notifyDataSetChanged();
                        mLecturesList.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isSubscribe(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
        final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(profileKey)){
                    mUnsubscribe.setVisibility(View.VISIBLE);
                }
                else {
                    mSubscribe.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
