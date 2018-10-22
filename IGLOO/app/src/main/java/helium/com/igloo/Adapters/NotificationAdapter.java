package helium.com.igloo.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Models.NotificationModel;
import helium.com.igloo.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private FirebaseStorage storage;
    private List<NotificationModel> notifications;
    private Context context;

    public NotificationAdapter(List<NotificationModel> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(context).
                inflate(R.layout.layout_notification, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {
        storage = FirebaseStorage.getInstance();
        NotificationModel notif = notifications.get(position);

        holder.notificationStreamer.setText(notif.getStreamer());
        holder.notificationTitle.setText(notif.getNotification_title());
        holder.notificationDescription.setText(notif.getNotification_description());
        //holder.notificationDate.setText(notif.get);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference userRef = databaseReference.child(notif.getStreamer_id());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String url = dataSnapshot.child("profileUrl").getValue(String.class);

                StorageReference storageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.profileImage.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{
        protected CircleImageView profileImage;
        protected TextView notificationStreamer;
        protected TextView notificationTitle;
        protected TextView notificationDescription;
        protected TextView notificationDate;

        public NotificationViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.notification_profile_image);
            notificationStreamer = itemView.findViewById(R.id.notification_streamer);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.notification_description);
            notificationDate = itemView.findViewById(R.id.notification_date);
        }
    }
}
