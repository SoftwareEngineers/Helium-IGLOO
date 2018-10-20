package helium.com.igloo.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.HomeActivity;
import helium.com.igloo.LectureActivity;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.ViewModel;
import helium.com.igloo.ProfileActivity;
import helium.com.igloo.R;
import helium.com.igloo.ViewArchiveActivity;
import helium.com.igloo.ViewLectureActivity;

public class LectureAdapter extends RecyclerView.Adapter<LectureAdapter.LectureViewHolder> {
    private List<LectureModel> lectures;
    private Context context;
    private FirebaseAuth auth;

    public LectureAdapter(List<LectureModel> lectures, Context context) {
        this.lectures = lectures;
        this.context = context;
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    @Override
    public void onBindViewHolder(final LectureViewHolder lectureViewHolder, int i) {
        final LectureModel p = lectures.get(i);
        lectureViewHolder.textTitle.setText(p.getTitle());
        lectureViewHolder.textDate.setText(p.getTime_created());

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        final FirebaseStorage storage = FirebaseStorage.getInstance();;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(p.getOwnerId()).child("name").getValue(String.class);
                String ownerUrl = dataSnapshot.child(p.getOwnerId()).child("profileUrl").getValue(String.class);

                String description = name + " " + Html.fromHtml("&#8226;") + " " + p.getViews() + " VIEWS";
                lectureViewHolder.textOwner.setText(description);

                StorageReference storageRef1 = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(ownerUrl);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef1.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    lectureViewHolder.imageOwner.setImageBitmap(bitmap);
                    }
                });

                StorageReference storageRef2 = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(p.getThumbnail());
                storageRef2.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        lectureViewHolder.imageView.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(!p.getPublic()){
            lectureViewHolder.imagePrivate.setVisibility(View.VISIBLE);
        }
        lectureViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("");
                databaseReference.child("Views").child(p.getId()).child(auth.getCurrentUser().getUid()).setValue(true);
                databaseReference.child("Views").child(p.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.child("Lectures").child(p.getId()).child("views").setValue(dataSnapshot.getChildrenCount());
                        p.setViews((int)dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(p.getLive()){
                    if(p.getPublic() || p.getOwner_id().equals(auth.getCurrentUser().getUid())){
                        Intent intent = new Intent(context,ViewLectureActivity.class);
                        intent.putExtra("key", p.getId());
                        context.startActivity(intent);
                    }
                    else{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final EditText password = new EditText(context);
                        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        alertDialog.setTitle("Enter password");
                        layout.addView(password);
                        alertDialog.setView(layout);
                        alertDialog.setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(password.getText().toString().equals(p.getPassword())){
                                    Intent intent = new Intent(context,ViewLectureActivity.class);
                                    intent.putExtra("key", p.getId());
                                    context.startActivity(intent);
                                }
                                else{
                                    Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                        });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();
                    }
                }
                else{
                    if(p.getPublic() || p.getOwner_id().equals(auth.getCurrentUser().getUid())){
                        Intent intent = new Intent(context,ViewArchiveActivity.class);
                        intent.putExtra("archiveID", p.getArchive_id());
                        intent.putExtra("key", p.getId());
                        context.startActivity(intent);
                    }
                    else{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final EditText password = new EditText(context);
                        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        alertDialog.setTitle("Enter password");
                        layout.addView(password);
                        alertDialog.setView(layout);
                        alertDialog.setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(password.getText().toString().equals(p.getPassword())){
                                    Intent intent = new Intent(context,ViewArchiveActivity.class);
                                    intent.putExtra("archiveID", p.getArchive_id());
                                    intent.putExtra("key", p.getId());
                                    context.startActivity(intent);
                                }
                                else{
                                    Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                        });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        });
    }

    @Override
public LectureAdapter.LectureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_video_container, viewGroup, false);
        return new LectureViewHolder(itemView);
    }


    public class LectureViewHolder extends RecyclerView.ViewHolder{

        protected CircleImageView imageOwner;
        protected TextView textOwner;
        protected TextView textTitle;
        protected TextView textDate;
        protected ImageView imageView;
        protected ImageView imagePrivate;
        protected View view;

        public LectureViewHolder(View v) {
            super(v);
            view = v;

            imageOwner = (CircleImageView) v.findViewById(R.id.video_profile);
            textOwner = (TextView) v.findViewById(R.id.video_description);
            textTitle = (TextView) v.findViewById(R.id.video_title);
            textDate = (TextView) v.findViewById(R.id.video_date_created);
            imageView  = (ImageView)v.findViewById(R.id.video_thumbnail);
            imagePrivate = (ImageView)v.findViewById(R.id.img_private);
        }
    }

}
