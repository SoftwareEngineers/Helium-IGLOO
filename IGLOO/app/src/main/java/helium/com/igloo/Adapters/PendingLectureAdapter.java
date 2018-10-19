package helium.com.igloo.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.UserModel;
import helium.com.igloo.R;
import helium.com.igloo.ViewArchiveActivity;
import helium.com.igloo.ViewLectureActivity;

public class PendingLectureAdapter extends RecyclerView.Adapter<PendingLectureAdapter.PendingLectureViewHolder> {
    private List<LectureModel> lectures;
    private Context context;
    private FirebaseAuth auth;
    private UserModel user;

    public PendingLectureAdapter(List<LectureModel> lectures, Context context) {
        this.lectures = lectures;
        this.context = context;
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    @Override
    public void onBindViewHolder(final PendingLectureViewHolder lectureViewHolder, int i) {
        final LectureModel p = lectures.get(i);
        lectureViewHolder.textTitle.setText(p.getTitle());
        lectureViewHolder.textViews.setText(p.getViews());

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.child(auth.getCurrentUser().getUid()).getValue(UserModel.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef1 = storage.getReferenceFromUrl("gs://igloo-0830.appspot.com/images/").child(user.getProfileUrl());
        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef1.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                lectureViewHolder.img_lecture.setImageBitmap(bitmap);
            }
        });

        lectureViewHolder.buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Lectures");
                databaseReference.child(p.getId()).child("available").setValue(true);
            }
        });

        lectureViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewArchiveActivity.class);
                intent.putExtra("archiveID", p.getArchive_id());
                intent.putExtra("key", p.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public PendingLectureAdapter.PendingLectureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_pending_lectures, viewGroup, false);
        return new PendingLectureViewHolder(itemView);
    }


    public class PendingLectureViewHolder extends RecyclerView.ViewHolder{

        protected ImageView img_lecture;
        protected TextView textTitle;
        protected TextView textViews;
        protected Button buttonUpload;
        protected View view;

        public PendingLectureViewHolder(View v) {
            super(v);
            view = v;
            img_lecture  = (ImageView)v.findViewById(R.id.img_lecture);
            textTitle = (TextView) v.findViewById(R.id.txt_title);
            textViews = (TextView) v.findViewById(R.id.txt_views);
            buttonUpload  = (Button) v.findViewById(R.id.btn_upload);
        }
    }

}
