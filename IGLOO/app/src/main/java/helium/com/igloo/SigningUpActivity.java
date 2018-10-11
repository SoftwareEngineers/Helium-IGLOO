package helium.com.igloo;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import helium.com.igloo.Models.UserModel;

public class SigningUpActivity extends AppCompatActivity {

    private TextInputEditText mName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mSignUpButton;
    private TextView mSwitch;

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
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SigningUpActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Toast.makeText(SigningUpActivity.this, "Authentication failed." + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(SigningUpActivity.this, "You have registered successfully", Toast.LENGTH_SHORT).show();
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                        UserModel user = new UserModel(name, email, password, "default_profile.png");
                        String userID = auth.getCurrentUser().getUid();

                        databaseReference.child(userID).setValue(user);

                        CountDown cd = new CountDown(100, 100);
                        cd.start();
                    }
                }
            });
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
            startActivity(new Intent(SigningUpActivity.this, TutorialActivity.class));
        }
    }

    private class SwitchPage implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(SigningUpActivity.this, SigningInActivity.class));
        }
    }
}
