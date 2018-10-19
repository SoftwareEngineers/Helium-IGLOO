package helium.com.igloo.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Models.SubscriptionModel;
import helium.com.igloo.R;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private FirebaseStorage storage;
    private List<SubscriptionModel> subscriptions;
    private Context context;

    public SubscriptionAdapter(List<SubscriptionModel> subscriptions, Context mContext) {
        this.subscriptions = subscriptions;
        context = mContext;
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    @Override
    public void onBindViewHolder(final SubscriptionViewHolder subscriptionViewHolder, int i) {
        storage = FirebaseStorage.getInstance();
        final SubscriptionModel subscription = subscriptions.get(i);
        String streamerID = subscription.getStreamer_id();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference userRef = databaseReference.child(streamerID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                double rating = dataSnapshot.child("rating").getValue(Double.class);
                String url = dataSnapshot.child("profileUrl").getValue(String.class);

                subscriptionViewHolder.userName.setText(name);
                subscriptionViewHolder.userRating.setRating((int)rating);

                StorageReference storageRef = storage.getReferenceFromUrl("gs://igloo-0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        subscriptionViewHolder.userPic.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public SubscriptionAdapter.SubscriptionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_subscriptions, viewGroup, false);

        return new SubscriptionAdapter.SubscriptionViewHolder(itemView);
    }


    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userPic;
        public TextView userName;
        public RatingBar userRating;

        public SubscriptionViewHolder(View v) {
            super(v);

            userPic = (CircleImageView) v.findViewById(R.id.subscriptions_pic);
            userName = (TextView) v.findViewById(R.id.subscriptions_name);
            userRating = (RatingBar) v.findViewById(R.id.subscriptions_rating);

            userRating.setNumStars(5);
        }
    }
}
