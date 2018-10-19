package helium.com.igloo;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import helium.com.igloo.Models.UserModel;

public class PaymentActivity extends AppCompatActivity {
    private EditText mTopup_amount;
    private TextView mAccount_balance;
    private FirebaseAuth auth;
    private String PaymentOption;
    private UserModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        auth = FirebaseAuth.getInstance();
        mTopup_amount = findViewById(R.id.txt_topup_amount);
        mAccount_balance = findViewById(R.id.txt_account_balance);


        setupUserAccount();

    }

    private void setupUserAccount(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                model =  dataSnapshot.getValue(UserModel.class);
                //int pTokens = dataSnapshot.child("tokens").getValue(Integer.class);
                int pTokens = (int)model.getTokens();

                mAccount_balance.setText(String.valueOf(pTokens));
                Toast.makeText(getApplicationContext(),String.valueOf(model.getTokens()),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void ExecuteMoneyToToken(View view) {
        PaymentOption = this.getString(R.string.money_to_token_dialog);
        ShowDialog(1);

    }

    public void ExecuteTokenToMoney(View view) {
        PaymentOption = this.getString(R.string.token_to_money_dialog);
        ShowDialog(0);
    }

    private int calculateRemainingBalance(){
        int balance = Integer.parseInt(mAccount_balance.getText().toString());
        int deduction = Integer.parseInt(mTopup_amount.getText().toString());
        deduction *= 5;
        int remaining_balance  = balance - deduction;

       return remaining_balance;

    }

    private double computeMoneyFromTokens(){
        int tokens = Integer.parseInt(mAccount_balance.getText().toString());
        int cashAmount = Integer.parseInt(mTopup_amount.getText().toString());
        cashAmount *= 5;
        if(tokens > 0 && tokens > cashAmount) {

            return (double) tokens - cashAmount;
        }
        else{
            return  0;
        }
    }

    public void ShowDialog(final int command) {
        LayoutInflater lay = LayoutInflater.from(PaymentActivity.this);
        View promptsView = lay.inflate(R.layout.layout_payment_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
        builder.setView(promptsView);

        TextView mPaymentDialog = promptsView.findViewById(R.id.txt_payment_dialog);
        mPaymentDialog.setText(PaymentOption);

        builder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (command == 1){


                                    int balance = Integer.parseInt(mTopup_amount.getText().toString());
                                    Toast.makeText(getApplicationContext(), "Successfully Topped up tokens!", Toast.LENGTH_LONG).show();
                                    UpdateUserBalance(balance,1);

                                }
                                else {
                                    double remaining_tokens = calculateRemainingBalance();
                                    double total_amount = computeMoneyFromTokens();
                                    if(total_amount >0){
                                        Toast.makeText(getApplicationContext(), "Successfully redeemed Cash!", Toast.LENGTH_LONG).show();
                                        UpdateUserBalance((int)remaining_tokens,2);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Not enough tokens to Convert!",Toast.LENGTH_LONG).show();
                                    }

                                }
                                dialog.dismiss();
                            }
                        });

        builder
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void UpdateUserBalance(int balance,int command){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());
        double final_balance;
        if(command == 1) {
            double current_balance = model.getTokens();
            final_balance = current_balance + balance;
        }
        else{
            final_balance = balance;
        }
        userRef.child("tokens").setValue(final_balance);
        mAccount_balance.setText(String.valueOf((int)final_balance));
    }
}
