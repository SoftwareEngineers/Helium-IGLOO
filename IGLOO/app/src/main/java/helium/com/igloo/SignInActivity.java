package helium.com.igloo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mSignInButton;
    private TextView mSwitch;
    ProgressDialog mProgressDialog;

    private FirebaseAuth auth;

    private String email;
    private String password;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            finish();
        }

        mEmail = (TextInputEditText) findViewById(R.id.email_signin_input);
        mPassword = (TextInputEditText) findViewById(R.id.password_signin_input);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSwitch = (TextView) findViewById(R.id.switch_sign_up_button);
        mProgressDialog = new ProgressDialog(this);

        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setTextAppearance(R.style.passwordFields);
        mSignInButton.setEnabled(false);

        mEmail.addTextChangedListener(new Change());
        mPassword.addTextChangedListener(new Change());
        mSignInButton.setOnClickListener(new SignIn());
        mSwitch.setOnClickListener(new SwitchPage());
    }

    private class Change implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            email = mEmail.getText().toString().trim();
            password = mPassword.getText().toString().trim();

            mSignInButton.setEnabled(!email.isEmpty() && !password.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class SignIn implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            mProgressDialog.setMessage("Signing in please wait....");
            mProgressDialog.show();

            try {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    if(!isOnline()){
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SignInActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SignInActivity.this, "Check your email and password", Toast.LENGTH_LONG).show();
                                    }
                                }
                                else {
                                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserModel");
                                    DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());
                                    DatabaseReference setupRef = userRef.child("setup");

                                    setupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Boolean value = dataSnapshot.getValue(Boolean.class);
                                            if (value == true) {
                                                CountDown cd = new CountDown(100, 100);
                                                cd.start();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
            }
            catch(Exception ie){}
        }
    }

    private class SwitchPage implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        }
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
            //startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        else {
            return false;
        }
    }
}