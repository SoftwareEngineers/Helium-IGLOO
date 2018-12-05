package helium.com.igloo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Format;
import java.util.Date;
import java.util.List;

import helium.com.igloo.Models.LectureModel;

public class CreatePublicLectureActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private EditText mTextTitle;
    private EditText mTextDescription;
    private Button mButtonCreate;
    private ImageButton mClose;
    private ImageView mThumbnail;
    private ImageButton mAddThumbnail;
    private TextView mThumbnailName;

    public static final int GET_FROM_GALLERY = 3;
    private Uri selectedImage;
    private String fileName;
    private int lecturesNo;

    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_public_lecture_view);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        progressBar = (ProgressBar)findViewById(R.id.prog_public_loading);
        mTextTitle = (EditText)findViewById(R.id.txt_public_lecture_title);
        mTextDescription = (EditText)findViewById(R.id.txt_public_description);
        mButtonCreate = (Button)findViewById(R.id.btn_create_public_lecture);
        mClose = (ImageButton) findViewById(R.id.close_public);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail_image);
        mAddThumbnail = (ImageButton) findViewById(R.id.thumbnail_button);
        mThumbnailName = (TextView) findViewById(R.id.thumbnail_name);

        mAddThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference("Lectures");
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTextTitle.getText().toString().equals("") &&
                        !mTextDescription.getText().toString().equals("")){
                    progressBar.setVisibility(View.VISIBLE);
                    final LectureModel lectureModel = new LectureModel();
                    lectureModel.setOwnerId(auth.getUid());
                    lectureModel.setTitle(mTextTitle.getText().toString());
                    lectureModel.setDescription(mTextDescription.getText().toString());
                    lectureModel.setPublic(true);
                    lectureModel.setAvailable(false);
                    lectureModel.setLive(false);
                    lectureModel.setIs_transcribed(false);
                    lectureModel.setTranscription("");
                    lectureModel.setUploadable(false);

                    StorageReference mStorageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/");;
                    if (selectedImage != null) {

                        StorageReference childRef = mStorageRef.child(fileName);

                        //uploading the image
                        UploadTask uploadTask = childRef.putFile(selectedImage);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                lectureModel.setThumbnail(fileName);

                                DateFormat dateFormat = new DateFormat();
                                lectureModel.setTime_created(String.valueOf(dateFormat.format("hh:mm a MMM-dd-yyyy", new Date())));
                                final String key = mDatabase.push().getKey();
                                lectureModel.setId(key);
                                lectureModel.setOwner_name(auth.getCurrentUser().getDisplayName());

                                mDatabase.child(key).setValue(lectureModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);

                                        update();

                                        Intent intent = new Intent(CreatePublicLectureActivity.this, LectureActivity.class);
                                        intent.putExtra("key", key);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreatePublicLectureActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreatePublicLectureActivity.this, "Failed to add thumbnail ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(CreatePublicLectureActivity.this, "Please fill up the empty field", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;
        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            final String newUrl = getRealPathFromURI(selectedImage);
            fileName = newUrl.substring(newUrl.lastIndexOf("/")+1);

            mThumbnailName.setText(fileName);
            mThumbnail.setImageBitmap(bitmap);
        }

    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void update(){
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference profileReference = userReference.child(auth.getCurrentUser().getUid());

        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lecturesNo = dataSnapshot.child("numberOfLectures").getValue(Integer.class);

                lecturesNo++;
                profileReference.child("numberOfLectures").setValue(lecturesNo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
