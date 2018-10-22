package helium.com.igloo;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import helium.com.igloo.Adapters.NotificationAdapter;
import helium.com.igloo.Models.NotificationModel;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout mEmptyNotifications;

    private RecyclerView notificationRecycleView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        auth = FirebaseAuth.getInstance();

        ImageButton mBack = (ImageButton) findViewById(R.id.notification_back);
        mEmptyNotifications = findViewById(R.id.no_notification);

        notificationRecycleView = findViewById(R.id.notification_recyclerview);
        notifications = new ArrayList<>();

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new NotificationAdapter(notifications,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationRecycleView.setLayoutManager(layoutManager);
        notificationRecycleView.setAdapter(adapter);

        loadNotifications();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }

    public void loadNotifications(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notifications");
        final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notifications.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    NotificationModel lecture = childSnapshot.getValue(NotificationModel.class);

                    notifications.add(lecture);
                    adapter.notifyDataSetChanged();
                    notificationRecycleView.smoothScrollToPosition(0);
                }

                if(notifications.size()>0) {
                    notificationRecycleView.setVisibility(View.VISIBLE);
                }
                else {
                    mEmptyNotifications.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
