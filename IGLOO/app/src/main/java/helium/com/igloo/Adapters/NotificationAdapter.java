package helium.com.igloo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Models.NotificationModel;
import helium.com.igloo.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
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
        NotificationModel notif = notifications.get(position);
        holder.notifationTitle.setText(notif.getNotification_title());
        holder.notifationDescription.setText(notif.getNotification_description());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{
        protected CircleImageView profileImage;
        protected TextView notifationTitle;
        protected TextView notifationDescription;
        public NotificationViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.notification_profile_image);
            notifationTitle = itemView.findViewById(R.id.notification_title);
            notifationDescription = itemView.findViewById(R.id.notification_description);
        }
    }
}
