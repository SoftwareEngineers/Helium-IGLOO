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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.LectureAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.R;

public class ArchiveLectureFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<LectureModel> lectures;
    private LectureAdapter lectureAdapter;
    private ProgressBar progressBar;

    public ArchiveLectureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_archive_lecture, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.pro_archive_lectures);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView)view.findViewById(R.id.rec_archive_lectures);
        lectures = new ArrayList<>();
        lectureAdapter = new LectureAdapter(lectures, super.getContext());
        recyclerView.setAdapter(lectureAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(super.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        loadLectures();

        return view;
    }
    public void loadLectures(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("available").getValue(Boolean.class) == true){
                        LectureModel lecture = childSnapshot.getValue(LectureModel.class);
//                        lecture.setOwner(childSnapshot.child("owner").getValue(String.class));
//                        lecture.setTitle(childSnapshot.child("title").getValue(String.class));
//                        lecture.setTime_created(childSnapshot.child("time_created").getValue(String.class));
                        lectures.add(lecture);
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
