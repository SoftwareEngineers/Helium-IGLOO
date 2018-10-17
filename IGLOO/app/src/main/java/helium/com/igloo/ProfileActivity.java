package helium.com.igloo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton mProfileBack;
    private CircleImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private RatingBar mRating;
    private TextView mSubscribers;
    private TextView mLectures;

    private FirebaseAuth auth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        mProfileBack = (ImageButton)findViewById(R.id.profile_back);
        mProfilePic = (CircleImageView) findViewById(R.id.profile_picture);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mRating = (RatingBar)findViewById(R.id.profile_rating);
        mSubscribers = (TextView) findViewById(R.id.profile__subscribers);
        mLectures = (TextView) findViewById(R.id.profile_lectures);

        mRating.setNumStars(5);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String url = dataSnapshot.child("profileUrl").getValue(String.class);
                double subscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);
                double lectures = dataSnapshot.child("numberOfLectures").getValue(Double.class);
                double rating = dataSnapshot.child("rating").getValue(Double.class);

                storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://igloo-0830.appspot.com/images/").child(url);
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
    }


}
