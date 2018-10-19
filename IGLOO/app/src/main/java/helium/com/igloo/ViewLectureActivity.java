package helium.com.igloo;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.Models.SubscriptionModel;

public class ViewLectureActivity extends AppCompatActivity implements Session.SessionListener {

    private Switch more;
    private ConstraintLayout details;
    private String key;
    private DatabaseReference databaseReference;
    private TextView textLectureTitle;
    private EditText textLectureDescription;
    private EditText textQuestion;
    private TextView textLectureOwner;
    private ImageView imageViewPrivate;
    private Button buttonAsk,buttonCall,buttonSubscribe;
    private FirebaseAuth auth;
    private FrameLayout viewLecture;
    private Session mSession;
    private ProgressBar progressBar;
    private Subscriber mSubscriber;
    private LectureModel lectureModel;

    private double numberOfSubscribers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lecture_view);

        Intent intent = getIntent();

        key = intent.getStringExtra("key");
        auth = FirebaseAuth.getInstance();

        more = findViewById(R.id.swi_more);
        progressBar = findViewById(R.id.prog_lecture);
        progressBar.setVisibility(View.VISIBLE);
        textLectureTitle = findViewById(R.id.txt_owner);
        viewLecture =  findViewById(R.id.view_lecture);
        textLectureDescription = findViewById(R.id.txt_lecture_description);
        textQuestion = findViewById(R.id.txt_question);
        textLectureOwner = findViewById(R.id.txt_lecture_owner);
        details = findViewById(R.id.lyout_more_details);
        imageViewPrivate = findViewById(R.id.img_private);
        buttonAsk = findViewById(R.id.btn_ask);
        buttonCall = findViewById(R.id.btn_call);
        buttonSubscribe = findViewById(R.id.btn_subscribe);

        buttonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lectureModel!=null){
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");
                    SubscriptionModel subscription = new SubscriptionModel(lectureModel.getOwner_name(), auth.getCurrentUser().getDisplayName(), lectureModel.getOwner_id(),auth.getCurrentUser().getUid(),"pending");
                    databaseReference.child(lectureModel.getOwner_id()).child(auth.getCurrentUser().getUid()).setValue(subscription);

                    final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
                    final DatabaseReference profilereference = userReference.child(lectureModel.getOwner_id());

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            numberOfSubscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    profilereference.child("numberOfSubscribers").setValue(numberOfSubscribers++);
                }
            }
        });
        buttonAsk.setOnClickListener(new Click());
        buttonCall.setOnClickListener(new Click());
        more.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    details.setVisibility(View.VISIBLE);
                else
                    details.setVisibility(View.GONE);
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lectureModel = dataSnapshot.child("Lectures").child(key).getValue(LectureModel.class);
                textLectureTitle.setText(lectureModel.getTitle());
                textLectureDescription.setText(lectureModel.getDescription());
                textLectureOwner.setText(dataSnapshot.child("Users").child(lectureModel.getOwnerId()).child("name").getValue(String.class));
                if(lectureModel.getPublic()){
                    imageViewPrivate.setVisibility(View.GONE);
                }
                else{
                    imageViewPrivate.setVisibility(View.VISIBLE);
                }
                fetchSessionConnectionData(lectureModel.getSession_id());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void fetchSessionConnectionData(final String sessionID) {
        RequestQueue reqQueue = Volley.newRequestQueue(ViewLectureActivity.this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/subscribe_session/" + sessionID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    final String apiKey = response.getString("api_key");
                    final String token = response.getString("token");

                    mSession = new Session.Builder(ViewLectureActivity.this, apiKey, sessionID).build();
                    mSession.setSessionListener(ViewLectureActivity.this);
                    mSession.connect(token);

                } catch (JSONException error) {
                    Toast.makeText(ViewLectureActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewLectureActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
        progressBar.setVisibility(View.GONE);
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(ViewLectureActivity.this, stream).build();
            mSession.subscribe(mSubscriber);
            viewLecture.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Intent intent = new Intent(ViewLectureActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }


    private class Click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
            String questionId = databaseReference.push().getKey();
            QuestionModel question = new QuestionModel();
            question.setQuestion(textQuestion.getText().toString());
            question.setLecture(key);
            question.setId(questionId);
            question.setIs_answered(false);
            DateFormat dateFormat = new DateFormat();
            question.setTime(String.valueOf(dateFormat.format("hh:mm a MMM-dd-yyyy", new Date())));
            question.setOwner_id(auth.getCurrentUser().getUid());
            if(v == buttonAsk)
                question.setIs_call(false);
            else if(v == buttonCall)
                question.setIs_call(true);
            databaseReference.child(questionId).setValue(question).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ViewLectureActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                    textQuestion.setText("");
                }
            });
        }
    }

    @Override
    public void onStop() {

        ViewLectureActivity.this.finish();
        if(mSession != null) {
            mSession.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(mSession != null) {
            mSession.disconnect();
        }
        ViewLectureActivity.this.finish();
        super.onBackPressed();
    }
}
