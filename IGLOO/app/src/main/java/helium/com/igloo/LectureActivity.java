package helium.com.igloo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;

public class LectureActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, Session.ArchiveListener {

    private TextView textLectureTitle;
    private TextView textLectureDescription;
    private Button buttonStartLecture;
    private RecyclerView recyclerView;
    private List<QuestionModel> questions;
    private QuestionAdapter questionAdapter;
    private String key;
    private FrameLayout mLectureView;
    private Publisher mPublisher;
    private Session mSession;
    private ProgressBar progressBar;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        Toast.makeText(this, key, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_lecture_view);
        progressBar = (ProgressBar)findViewById(R.id.prog_lecture);
        mLectureView = (FrameLayout) findViewById(R.id.frm_lecture_view);
        textLectureTitle = (TextView)findViewById(R.id.txt_lecture_title);
        textLectureDescription = (TextView)findViewById(R.id.txt_lecture_description);
        buttonStartLecture = (Button)findViewById(R.id.btn_start_lecture);
        buttonStartLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                startLecture();
            }
        });
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LectureModel mLectureModel = dataSnapshot.child(key).getValue(LectureModel.class);
                textLectureTitle.setText(mLectureModel.getTitle());
                textLectureDescription.setText(mLectureModel.getDescription());
                Toast.makeText(LectureActivity.this, mLectureModel.getTitle(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        storageReference = FirebaseStorage.getInstance().getReference("Lectures");
        recyclerView = (RecyclerView)findViewById(R.id.rec_questions);
        questions = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questions, this);
        recyclerView.setAdapter(questionAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        loadQuestions();
    }

    public void startLecture(){
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/create_session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    final String api_key = response.getString("api_key");
                    final String session_id = response.getString("session_id");
                    final String token = response.getString("token");
                    mSession = new Session.Builder(LectureActivity.this, api_key, session_id).build();
                    mSession.setSessionListener(LectureActivity.this);
                    mSession.setArchiveListener(LectureActivity.this);
                    mSession.connect(token);
                } catch (JSONException error) {
                    Toast.makeText(LectureActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LectureActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

    }

    @Override
    public void onConnected(Session session) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.child(key).child("session_id").setValue(session.getSessionId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReference.child(key).child("live").setValue(true);
            }
        });
        mPublisher = new Publisher.Builder(LectureActivity.this ).build();
        mPublisher.setPublisherListener(this);
        progressBar.setVisibility(View.GONE);
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mLectureView.addView(mPublisher.getView(), 0);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.child(key).child("live").setValue(false);
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Toast.makeText(LectureActivity.this, opentokError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void loadQuestions(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
        databaseReference.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questions.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Toast.makeText(LectureActivity.this,childSnapshot.child("lecture").getValue(String.class) + " == " + key, Toast.LENGTH_SHORT).show();
                    if(childSnapshot.child("lecture").getValue(String.class).equals(key) && !childSnapshot.child("is_answered").getValue(Boolean.class)){
                        QuestionModel question = childSnapshot.getValue(QuestionModel.class);
                        questions.add(question);
                        questionAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Toast.makeText(LectureActivity.this, "Lecture started successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.child(key).child("live").setValue(false);
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Toast.makeText(LectureActivity.this, opentokError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onArchiveStarted(Session session, String archiveID, String archiveName) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.child(key).child("archive_id").setValue(archiveID);
    }

    @Override
    public void onArchiveStopped(Session session, String s) {
    }

    @Override
    public void onStop() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.child(key).child("live").setValue(false);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LectureActivity.this);
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("Your lecture will be terminated. Are you sure you want to continue?");
        alertDialog.setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(LectureActivity.this, HomeActivity.class);
                startActivity(intent);
                LectureActivity.this.finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}
