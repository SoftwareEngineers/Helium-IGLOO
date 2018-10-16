package helium.com.igloo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.MediaController;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Models.QuestionModel;

public class ViewArchiveActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private ProgressBar progressBar;
    private Uri mUrl;
    private RecyclerView mRecycleViewQuestions;
    private List<QuestionModel> questions;
    private QuestionAdapter questionAdapter;
    private String mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_archive);
        videoView = (VideoView)findViewById(R.id.view_lecture);
        progressBar = (ProgressBar)findViewById(R.id.prog_archive);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String archiveID = intent.getStringExtra("archiveID");
        mKey = intent.getStringExtra("key");
        playArchive(archiveID);
        mRecycleViewQuestions = (RecyclerView)findViewById(R.id.rec_questions);
        questions = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questions, this);
        mRecycleViewQuestions.setAdapter(questionAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewQuestions.setLayoutManager(layoutManager);
        loadQuestions();
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
                    Uri video = Uri.parse( response.getString("url"));
                    Log.e("Opentok Archive", "Archive starting");
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(video);

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        public void onPrepared(MediaPlayer mp) {
                            progressBar.setVisibility(View.GONE);
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
}
