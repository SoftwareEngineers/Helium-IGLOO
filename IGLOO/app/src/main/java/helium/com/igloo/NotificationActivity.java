package helium.com.igloo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
        notifications.add(new NotificationModel("asdas","asdas","asdasdasd","asdsa","asdas","dsadas","sdasdasd"));
        adapter = new NotificationAdapter(notifications,this);
        adapter.notifyDataSetChanged();
        notificationRecycleView.smoothScrollToPosition(0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationRecycleView.smoothScrollToPosition(0);
        notificationRecycleView.setLayoutManager(layoutManager);
        notificationRecycleView.setAdapter(adapter);


    }
}
