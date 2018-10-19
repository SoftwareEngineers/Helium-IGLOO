package helium.com.igloo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helium.com.igloo.Adapters.LectureAdapter;
import helium.com.igloo.Adapters.PendingLectureAdapter;
import helium.com.igloo.Models.LectureModel;

public class PendingLecturesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<LectureModel> lectures;
    private PendingLectureAdapter lectureAdapter;
    private ProgressBar progressBar;
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_lectures);
        auth = FirebaseAuth.getInstance();
        recyclerView = (RecyclerView)findViewById(R.id.rec_lectures);
        lectures = new ArrayList<>();
        lectureAdapter = new PendingLectureAdapter(lectures, context);
        recyclerView.setAdapter(lectureAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        loadLectures();
    }

    public void loadLectures(){
        databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.orderByChild("time_created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lectures.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("owner_id").getValue(String.class).equals(auth.getCurrentUser().getUid()) && !childSnapshot.child("available").getValue(Boolean.class) && !childSnapshot.child("uploadable").getValue(Boolean.class)){
                        LectureModel lecture = childSnapshot.getValue(LectureModel.class);
                        checkStatus(lecture.getId(), lecture.getArchive_id());
                    }
                }

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("owner_id").getValue(String.class).equals(auth.getCurrentUser().getUid()) && !childSnapshot.child("available").getValue(Boolean.class) && childSnapshot.child("uploadable").getValue(Boolean.class)){
                        LectureModel lecture = childSnapshot.getValue(LectureModel.class);
                        lectures.add(lecture);
                        Collections.reverse(lectures);
                        lectureAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkStatus(final String key, String archiveID){
        RequestQueue reqQueue = Volley.newRequestQueue(context);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/"+archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("status").toString().equals("available")){
                        databaseReference.child(key).child("uploadable").setValue(true);
                    }
                } catch (JSONException error) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }
}
