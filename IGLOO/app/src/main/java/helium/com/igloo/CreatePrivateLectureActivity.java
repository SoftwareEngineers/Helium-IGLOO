package helium.com.igloo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import helium.com.igloo.Models.LectureModel;

public class CreatePrivateLectureActivity extends AppCompatActivity {

    private Button mButtonCreate;
    private EditText mTextTitle;
    private EditText mTextPassword;
    private EditText mTextDescription;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private ImageButton mClose;
    private ImageView mThumbnail;
    private ImageButton mAddThumbnail;
    private TextView mThumbnailName;

    public static final int GET_FROM_GALLERY = 3;
    private Uri selectedImage;
    private String fileName;

    private FirebaseAuth auth;
    private FirebaseStorage storage;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_private_lecture_view);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        progressBar = (ProgressBar)findViewById(R.id.prog_private_loading);
        mTextTitle = (EditText)findViewById(R.id.txt_private_lecture_title);
        mTextDescription = (EditText)findViewById(R.id.txt_private_lecture_description);
        mTextPassword = (EditText)findViewById(R.id.txt_private_lecture_password);
        mButtonCreate = (Button)findViewById(R.id.btn_create_private_lecture);
        mDatabase = FirebaseDatabase.getInstance().getReference("Lectures");
        mClose = (ImageButton) findViewById(R.id.close_private);
        mThumbnail = (ImageView) findViewById(R.id.private_thumbnail_image);
        mAddThumbnail = (ImageButton) findViewById(R.id.private_thumbnail_button);
        mThumbnailName = (TextView) findViewById(R.id.private_thumbnail_name);

        mTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mTextPassword.setTextAppearance(R.style.passwordFields);

        mAddThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTextTitle.getText().toString() != "" &&
                        mTextDescription.getText().toString() != "" &&
                        mTextPassword.getText().toString() != ""){
                    progressBar.setVisibility(View.VISIBLE);
                    final LectureModel lectureModel = new LectureModel();
                    lectureModel.setOwnerId(auth.getUid());
                    lectureModel.setTitle(mTextTitle.getText().toString());
                    lectureModel.setDescription(mTextDescription.getText().toString());
                    lectureModel.setPassword(mTextPassword.getText().toString());
                    lectureModel.setPublic(false);
                    lectureModel.setAvailable(false);
                    lectureModel.setLive(false);
                    lectureModel.setIs_transcribed(false);
                    lectureModel.setTranscription("");

                    StorageReference mStorageRef = storage.getReferenceFromUrl("gs://igloo-0830.appspot.com/images/");;
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
                                        Intent intent = new Intent(CreatePrivateLectureActivity.this, LectureActivity.class);
                                        intent.putExtra("key", key);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CreatePrivateLectureActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreatePrivateLectureActivity.this, "Failed to upload thumbnail ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
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
}
