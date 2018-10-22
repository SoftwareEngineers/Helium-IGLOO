package helium.com.igloo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView mProfilePic;
    private ImageButton mChangePic;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private LinearLayout mEditUsername;
    private LinearLayout mUpdateEMail;
    private LinearLayout mChangePassword;

    public static final int GET_FROM_GALLERY = 3;
    private String fileName;

    private FirebaseAuth auth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        ImageButton mBack = (ImageButton) findViewById(R.id.settings_back);
        mProfilePic = (CircleImageView) findViewById(R.id.settings_pic);
        mChangePic = (ImageButton) findViewById(R.id.change_profile_pic);
        mProfileName = (TextView) findViewById(R.id.settings_name);
        mProfileEmail = (TextView) findViewById(R.id.settings_email);
        mEditUsername = (LinearLayout) findViewById(R.id.edit_username);
        mUpdateEMail = (LinearLayout) findViewById(R.id.update_email);
        mChangePassword = (LinearLayout) findViewById(R.id.change_password);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        mEditUsername.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openNameDialog();
            }
        });

        mUpdateEMail.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openEmailDialog();
            }
        });

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openPasswordDialog();
            }
        });

        loadInfo();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }

    private void loadInfo(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String url = dataSnapshot.child("profileUrl").getValue(String.class);
                double subscribers = dataSnapshot.child("numberOfSubscribers").getValue(Double.class);
                double lectures = dataSnapshot.child("numberOfLectures").getValue(Double.class);
                double rating = dataSnapshot.child("totalRatings").getValue(Double.class);
                int tokens = dataSnapshot.child("tokens").getValue(Integer.class);

                storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mProfilePic.setImageBitmap(bitmap);
                    }
                });

                mProfileName.setText(name);
                mProfileEmail.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;
        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
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

            mProfilePic.setImageBitmap(bitmap);

//            StorageReference mStorageRef = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/");;
//            if (selectedImage != null) {
//
//                StorageReference childRef = mStorageRef.child(fileName);
//                UploadTask uploadTask = childRef.putFile(selectedImage);
//
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(SettingsActivity.this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SettingsActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.layout_edit_name, null);
        LinearLayout layout = (LinearLayout) subView.findViewById(R.id.edit_name_layout);
        layout.setBackgroundResource(R.drawable.ellipse_dialog);

        final TextInputEditText newName = (TextInputEditText) subView.findViewById(R.id.edit_name);
        final TextInputEditText confirmPassword = (TextInputEditText)subView.findViewById(R.id.edit_name_password);
        Button confirm = (Button) subView.findViewById(R.id.edit_name_confirm);
        Button cancel = (Button) subView.findViewById(R.id.edit_name_cancel);

        confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPassword.setTextAppearance(R.style.passwordFields);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(subView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.85);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = newName.getText().toString().trim();
                final String password = confirmPassword.getText().toString().trim();

                if(!name.isEmpty() && !password.equals("")){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("password").getValue(String.class).equals(password)){
                                userReference.child("name").setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        alertDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Name successfully change", Toast.LENGTH_SHORT).show();
                                        mProfileName.setText(name);
                                    }
                                });
                            }
                            else {
                                Toast.makeText(SettingsActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please fill up empty fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openPasswordDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.layout_change_password, null);
        LinearLayout layout = (LinearLayout) subView.findViewById(R.id.change_password_layout);
        layout.setBackgroundResource(R.drawable.ellipse_dialog);

        final TextInputEditText oldPassword = (TextInputEditText) subView.findViewById(R.id.old_password);
        final TextInputEditText newPassword = (TextInputEditText)subView.findViewById(R.id.new_password);
        Button confirm = (Button) subView.findViewById(R.id.change_password_confirm);
        Button cancel = (Button) subView.findViewById(R.id.change_password_cancel);

        oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPassword.setTextAppearance(R.style.passwordFields);
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPassword.setTextAppearance(R.style.passwordFields);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(subView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.85);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oldPass = oldPassword.getText().toString().trim();
                final String newPass = newPassword.getText().toString().trim();

                if(!oldPass.isEmpty() && !newPass.isEmpty()){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("password").getValue(String.class).equals(oldPass)){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userReference.child("password").setValue(newPass);
                                            Toast.makeText(SettingsActivity.this, "Password successfully updated", Toast.LENGTH_SHORT).show();
                                            alertDialog.dismiss();
                                        }
                                        else {
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                            if(errorCode.equals("ERROR_WEAK_PASSWORD")){
                                                Toast.makeText(SettingsActivity.this, "The password is invalid it must 6 characters at least.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                            }
                            else {
                                Toast.makeText(SettingsActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please fill up empty fields", Toast.LENGTH_SHORT).show();

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openEmailDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.layout_update_email, null);
        LinearLayout layout = (LinearLayout) subView.findViewById(R.id.update_email_layout);
        layout.setBackgroundResource(R.drawable.ellipse_dialog);

        final TextInputEditText newEmail = (TextInputEditText) subView.findViewById(R.id.update_email);
        final TextInputEditText confirmPassword = (TextInputEditText)subView.findViewById(R.id.update_email_password);
        Button confirm = (Button) subView.findViewById(R.id.update_email_confirm);
        Button cancel = (Button) subView.findViewById(R.id.update_email_cancel);

        confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPassword.setTextAppearance(R.style.passwordFields);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(subView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.85);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = newEmail.getText().toString().trim();
                final String password = confirmPassword.getText().toString().trim();

                if(!email.isEmpty() && !password.equals("")){
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                    final DatabaseReference userReference = databaseReference.child(auth.getCurrentUser().getUid());

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("password").getValue(String.class).equals(password)){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userReference.child("email").setValue(email);
                                            alertDialog.dismiss();
                                            Toast.makeText(SettingsActivity.this, "Email successfully updated", Toast.LENGTH_SHORT).show();
                                            mProfileEmail.setText(email);
                                        }
                                        else {
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                            switch (errorCode) {

                                                case "ERROR_INVALID_EMAIL":
                                                    Toast.makeText(SettingsActivity.this, "The email address is invalid.", Toast.LENGTH_LONG).show();
                                                    break;

                                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                                    Toast.makeText(SettingsActivity.this, "The email address already exist.", Toast.LENGTH_LONG).show();
                                                    break;
                                            }
                                        }
                                    }
                                });
                            }
                            else {
                                Toast.makeText(SettingsActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please fill up empty fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
}
