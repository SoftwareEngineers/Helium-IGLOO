package helium.com.igloo.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helium.com.igloo.Adapters.LectureAdapter;
import helium.com.igloo.LectureActivity;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.R;

public class ArchiveLectureFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<LectureModel> lectures;
    private LectureAdapter lectureAdapter;
    private ProgressBar progressBar;
    private Context context;

    public ArchiveLectureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_archive_lecture, container, false);
        context = super.getContext();
        progressBar = (ProgressBar)view.findViewById(R.id.pro_archive_lectures);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView)view.findViewById(R.id.rec_archive_lectures);
        lectures = new ArrayList<>();
        lectureAdapter = new LectureAdapter(lectures, context);
        recyclerView.setAdapter(lectureAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        loadLectures();

        return view;
    }

    @Override
    public void onResume(){
        Toast.makeText(context, "called archived", Toast.LENGTH_SHORT).show();
        loadLectures();
        super.onResume();
    }

    public void loadLectures(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        final Context context = super.getContext();
        databaseReference.orderByChild("time_created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lectures.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(!childSnapshot.child("available").getValue(Boolean.class)){
                        checkStatus(databaseReference, childSnapshot.getKey(), childSnapshot.child("archive_id").getValue(String.class));
                    }
                }

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("available").getValue(Boolean.class) == true){
                        LectureModel lecture = childSnapshot.getValue(LectureModel.class);
//                        lecture.setOwner(childSnapshot.child("owner").getValue(String.class));
//                        lecture.setTitle(childSnapshot.child("title").getValue(String.class));
//                        lecture.setTime_created(childSnapshot.child("time_created").getValue(String.class));
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
        progressBar.setVisibility(View.GONE);
    }

    public void checkStatus(final DatabaseReference databaseReference, final String key, String archiveID){
        RequestQueue reqQueue = Volley.newRequestQueue(context);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/"+archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("status").toString().equals("available")){
                        databaseReference.child(key).child("available").setValue(true);
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
