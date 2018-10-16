package helium.com.igloo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helium.com.igloo.Adapters.LectureAdapter;
import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.R;

public class LiveLectureFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<LectureModel> lectures;
    private LectureAdapter lectureAdapter;
    private ProgressBar progressBar;

    public LiveLectureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_live_lecture, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.pro_live_lectures);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView)view.findViewById(R.id.rec_live_lectures);
        lectures = new ArrayList<>();
        lectureAdapter = new LectureAdapter(lectures, super.getContext());
        recyclerView.setAdapter(lectureAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(super.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        loadLectures();

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        loadLectures();
    }

    public void loadLectures(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.orderByChild("time_created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lectures.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (childSnapshot.child("live").getValue(Boolean.class) == true) {
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
        progressBar.setVisibility(View.GONE);
    }

}
