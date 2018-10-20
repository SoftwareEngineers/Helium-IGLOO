package helium.com.igloo;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.NotificationAdapter;
import helium.com.igloo.Models.NotificationModel;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notificationRecycleView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        notificationRecycleView = findViewById(R.id.notification_recyclerview);
        notifications = new ArrayList<>();
        notifications =  (List<NotificationModel>) getIntent().getSerializableExtra("notifications");
        if(notifications.size()>0){
            Toast.makeText(this,"not empty",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"empty notifications",Toast.LENGTH_SHORT).show();
        }
        adapter = new NotificationAdapter(notifications,this);
        adapter.notifyDataSetChanged();
        notificationRecycleView.smoothScrollToPosition(0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationRecycleView.smoothScrollToPosition(0);
        notificationRecycleView.setLayoutManager(layoutManager);
        notificationRecycleView.setAdapter(adapter);
    }
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }
}
