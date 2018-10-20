package helium.com.igloo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MediaController;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Adapters.TransciptionAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.Models.SubscriptionModel;
import helium.com.igloo.Models.TranscriptionModel;
import helium.com.igloo.Models.UserModel;


public class ViewArchiveActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private ProgressBar progressBar;
    private RecyclerView mRecycleViewQuestions;
    private List<QuestionModel> questions;
    private QuestionAdapter questionAdapter;
    private String mKey,archiveID;
    private LectureModel lecture;

    private TextView textOwner;
    private TextView textSubscribers;
    private TextView textTitle;
    private TextView textViews;
    private FirebaseAuth auth;
    private CircleImageView mLecturer;
    private Button mSubscribe;
    private Button mUnsubscribe;

    private RecyclerView mRecycleViewTranscripts;
    private List<TranscriptionModel> transcripts;
    private TransciptionAdapter transciptionAdapter;
    private String transcribedText;
    private double numberOfSubscribers = 0;
    private AutoCompleteTextView txtTranscriptSearch;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_archive);


        videoView = findViewById(R.id.view_lecture);
        progressBar = findViewById(R.id.prog_archive);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        archiveID = intent.getStringExtra("archiveID");
        mKey = intent.getStringExtra("key");

        textOwner = (TextView)findViewById(R.id.txt_owner);
        textSubscribers = (TextView)findViewById(R.id.txt_subscribers);
        textTitle = (TextView)findViewById(R.id.txt_title);
        textViews = (TextView)findViewById(R.id.txt_views);
        mLecturer = (CircleImageView)findViewById(R.id.img_owner);
        mSubscribe = (Button) findViewById(R.id.btn_archive_subscribe);
        mUnsubscribe = (Button) findViewById(R.id.btn_archive_unsubscribe);
        txtTranscriptSearch = findViewById(R.id.txtVideoSearch);
        
        auth = FirebaseAuth.getInstance();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("");
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        databaseReference.child("Lectures").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lecture = dataSnapshot.child(mKey).getValue(LectureModel.class);
                textTitle.setText(lecture.getTitle());
                textViews.setText(lecture.getViews() + " Views");
                String url = dataSnapshot.child(mKey).child("thumbnail").getValue(String.class);

                StorageReference storageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                        videoView.setBackground(bitmapDrawable);
                    }
                });
                transcribedText = lecture.getTranscription();
                StringTokenizer st = new StringTokenizer(transcribedText," ");
                transcripts = new ArrayList<>();
                final List<String> words = new ArrayList<>();
                String word;
                int time = 0;
                int i = 0;
                while (st.hasMoreElements()){
                    word = st.nextElement().toString();
                    time = Integer.parseInt(st.nextElement().toString());
                    transcripts.add(new TranscriptionModel(word,time,i++));
                    words.add(word);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewArchiveActivity.this,android.R.layout.simple_list_item_1,words);
                txtTranscriptSearch.setAdapter(adapter);
                txtTranscriptSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int index =words.indexOf(parent.getItemAtPosition(position));
                        int time = transcripts.get(index).getTime();
                        if(mediaPlayer!=null){
                            mediaPlayer.seekTo(time);
                        }
                        Toast.makeText(ViewArchiveActivity.this,transcripts.get(index).getTime()+"",Toast.LENGTH_LONG).show();

                    }
                });
               playArchive(archiveID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.child(lecture.getOwner_id()).getValue(UserModel.class);
                textOwner.setText(user.getName());
                textSubscribers.setText(user.getNumberOfSubscribers() + " Subscribers");

                StorageReference storageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(user.getProfileUrl());
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mLecturer.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
                final DatabaseReference subscriptionReference = databaseReference.child(auth.getCurrentUser().getUid());

                SubscriptionModel subscription = new SubscriptionModel(lecture.getOwner_name(), auth.getCurrentUser().getDisplayName(), lecture.getOwner_id(),auth.getCurrentUser().getUid(),"pending");
                subscriptionReference.child(lecture.getOwner_id()).setValue(subscription);

                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
                final DatabaseReference profileReference = userReference.child(lecture.getOwner_id());

                profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        numberOfSubscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);

                        numberOfSubscribers++;
                        textSubscribers.setText(Integer.toString((int)numberOfSubscribers));
                        profileReference.child("numberOfSubscribers").setValue(numberOfSubscribers);
                        mSubscribe.setVisibility(View.GONE);
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
                subscriptionReference.child(lecture.getOwner_id()).getRef().removeValue();

                final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
                final DatabaseReference profileReference = userReference.child(lecture.getOwner_id());

                profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        numberOfSubscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);

                        numberOfSubscribers--;
                        textSubscribers.setText(Integer.toString((int)numberOfSubscribers));
                        profileReference.child("numberOfSubscribers").setValue(numberOfSubscribers);
                        mUnsubscribe.setVisibility(View.INVISIBLE);
                        mSubscribe.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mRecycleViewQuestions = (RecyclerView) findViewById(R.id.rec_questions);
        questions = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questions, this, videoView);
        mRecycleViewQuestions.setAdapter(questionAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewQuestions.setLayoutManager(layoutManager);
        loadQuestions();
        isSubscribe();
    }


    public void playArchive(String archiveID){
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/" + archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    mediaController = new MediaController(ViewArchiveActivity.this);
                    mediaController.setAnchorView(videoView);
                    final Uri video = Uri.parse(response.getString("url"));

                    Log.e("Opentok Archive", "Archive starting");
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(video);
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        public void onPrepared(final MediaPlayer mp) {
                            mediaPlayer = mp;
                            questionAdapter.getMediaPlayer(mp);
                            progressBar.setVisibility(View.GONE);
                            videoView.setBackground(null);
                            videoView.start();
                            Log.e("Opentok Archive", "Archive started");
                        }
                    });

                } catch (Exception e) {
                    System.out.println("Video Play Error :" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewArchiveActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void loadQuestions(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
        databaseReference.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questions.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("lecture").getValue(String.class).equals(mKey) && childSnapshot.child("is_answered").getValue(Boolean.class)){
                        QuestionModel question = childSnapshot.getValue(QuestionModel.class);
                        questions.add(question);
                        questionAdapter.notifyDataSetChanged();
                        mRecycleViewQuestions.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void isSubscribe(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
        final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(lecture.getOwner_id())){
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
