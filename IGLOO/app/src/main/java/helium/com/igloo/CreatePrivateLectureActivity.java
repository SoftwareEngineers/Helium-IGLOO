package helium.com.igloo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

import helium.com.igloo.Models.LectureModel;

public class CreatePrivateLectureActivity extends AppCompatActivity {

    private Button mButtonCreate;
    private EditText mTextTitle;
    private EditText mTextPassword;
    private EditText mTextDescription;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_private_lecture_view);
        auth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar)findViewById(R.id.prog_private_loading);
        mTextTitle = (EditText)findViewById(R.id.txt_private_lecture_title);
        mTextDescription = (EditText)findViewById(R.id.txt_private_lecture_description);
        mTextPassword = (EditText)findViewById(R.id.txt_private_lecture_password);
        mButtonCreate = (Button)findViewById(R.id.btn_create_private_lecture);
        mDatabase = FirebaseDatabase.getInstance().getReference("Lectures");
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
                    lectureModel.setTime_created(String.valueOf(new Date()));
                    final String key = mDatabase.push().getKey();
                    lectureModel.setId(key);
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
            }
        });
    }
}
