package helium.com.igloo;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;

public class LectureView extends AppCompatActivity implements Session.SessionListener{

    private TextView textLectureTitle;
    private TextView textLectureDescription;
    private Button buttonStartLecture;
    private RecyclerView recyclerView;
    private List<QuestionModel> questions;
    private QuestionAdapter questionAdapter;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        Toast.makeText(this, key, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_lecture_view);
        textLectureTitle = (TextView)findViewById(R.id.txt_lecture_title);
        textLectureDescription = (TextView)findViewById(R.id.txt_lecture_description);
        buttonStartLecture = (Button)findViewById(R.id.btn_start_lecture);
        buttonStartLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LectureModel mLectureModel = dataSnapshot.child(key).getValue(LectureModel.class);
                textLectureTitle.setText(mLectureModel.getTitle());
                textLectureDescription.setText(mLectureModel.getDescription());
                Toast.makeText(LectureView.this, mLectureModel.getTitle(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com" + "/create_session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    final String api_key = response.getString("api_key");
                    final String session_id = response.getString("session_id");
                    final String token = response.getString("token");
                    final Session mSession = new Session.Builder(LectureView.this, api_key, session_id).build();
                    mSession.setSessionListener(LectureView.this);
                    mSession.connect(token);
                    databaseReference.child(key).child("id").setValue(session_id);
                    databaseReference.child(key).child("isLive").setValue(true);
                } catch (JSONException error) {
                    Toast.makeText(LectureView.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LectureView.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

    }

    @Override
    public void onConnected(Session session) {

    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    public void loadQuestions(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Toast.makeText(LectureView.this,childSnapshot.child("lecture").getValue(String.class) + " == " + key, Toast.LENGTH_SHORT).show();
                    if(childSnapshot.child("lecture").getValue(String.class).equals(key)){
                        QuestionModel question = new QuestionModel();
                        question.setOwner_id(childSnapshot.child("owner_id").getValue(String.class));
                        question.setQuestion(childSnapshot.child("question").getValue(String.class));
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
}
