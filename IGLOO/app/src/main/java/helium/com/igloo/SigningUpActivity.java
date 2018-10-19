package helium.com.igloo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import helium.com.igloo.Models.UserModel;

public class SigningUpActivity extends AppCompatActivity {

    private TextInputEditText mName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mSignUpButton;
    private TextView mSwitch;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth auth;

    private String name;
    private String email;
    private String password;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing_up);

        auth = FirebaseAuth.getInstance();

        mName = (TextInputEditText) findViewById(R.id.name_signup_input);
        mEmail = (TextInputEditText) findViewById(R.id.email_signup_input);
        mPassword = (TextInputEditText) findViewById(R.id.password_signup_input);
        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSwitch = (TextView) findViewById(R.id.switch_sign_in_button);
        mProgressDialog = new ProgressDialog(this);

        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setTextAppearance(R.style.passwordFields);
        mSignUpButton.setEnabled(false);

        mName.addTextChangedListener(new Change());
        mEmail.addTextChangedListener(new Change());
        mPassword.addTextChangedListener(new Change());
        mSwitch.setOnClickListener(new SwitchPage());
        mSignUpButton.setOnClickListener(new SignUp());
    }

    private class Change implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            name = mName.getText().toString().trim();
            email = mEmail.getText().toString().trim();
            password = mPassword.getText().toString().trim();

            mSignUpButton.setEnabled(!name.isEmpty() && !email.isEmpty() && !password.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class SignUp implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mProgressDialog.setMessage("Signing in please wait....");
            mProgressDialog.show();

            if(!isOnline()){
                mProgressDialog.dismiss();
                Toast.makeText(SigningUpActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SigningUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {

                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            switch (errorCode) {

                                case "ERROR_INVALID_EMAIL":
                                    Toast.makeText(SigningUpActivity.this, "The email address is invalid.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    Toast.makeText(SigningUpActivity.this, "The email address already exist.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    Toast.makeText(SigningUpActivity.this, "The password is invalid it must 6 characters at least.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                        else {

                            Toast.makeText(SigningUpActivity.this, "You have registered successfully", Toast.LENGTH_SHORT).show();
                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                            UserModel user = new UserModel(name, email, password, "default_profile.png");
                            String userID = auth.getCurrentUser().getUid();

                            databaseReference.child(userID).setValue(user);
                            saveUser(email, password);
                            FirebaseUser tempuser = auth.getCurrentUser();
                            if(user!=null){
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name).build();
                                tempuser.updateProfile(profileUpdates);
                            }
                            CountDown cd = new CountDown(100, 100);
                            cd.start();
                        }
                    }
                });
            }
        }
    }

    private void saveUser(String email,String password){
        SharedPreferences settings = getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();
    }

    public class CountDown extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture,countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mProgressDialog.dismiss();
            startActivity(new Intent(SigningUpActivity.this, TutorialActivity.class));
            overridePendingTransition(R.transition.slide_in_right,R.transition.slide_out_left);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class SwitchPage implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(SigningUpActivity.this, SigningInActivity.class));
            overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
        }
    }
}
