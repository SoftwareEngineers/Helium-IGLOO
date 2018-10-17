package helium.com.igloo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import org.w3c.dom.Text;

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
                //Toast.makeText(getApplicationContext(),model.getName(),Toast.LENGTH_LONG).show();
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
        int remaining_balance  = balance % deduction;

       return remaining_balance;

    }


    private double computeMoneyFromTokens(){
        int tokens = Integer.parseInt(mAccount_balance.getText().toString());
        int cashAmount = Integer.parseInt(mTopup_amount.getText().toString());
        if(tokens > 0 && tokens > cashAmount) {
            return (double) tokens / cashAmount;
        }
        else{
            return  0;
        }
    }

    public void ShowDialog(final int command) {
        LayoutInflater lay = LayoutInflater.from(PaymentActivity.this);
        View promptsView = lay.inflate(R.layout.payment_alertdialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
        builder.setView(promptsView);

        TextView mPaymentDialog = promptsView.findViewById(R.id.txt_payment_dialog);
        mPaymentDialog.setText(PaymentOption);

        builder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //UpdateUserTokens(5);
                                if (command == 1){
                                    Toast.makeText(getApplicationContext(), "Money To Token", Toast.LENGTH_LONG).show();
                                    double total_amount = computeMoneyFromTokens();
                                    if(total_amount >0){
                                        Toast.makeText(getApplicationContext(), "Success fully Recieved Money To Token", Toast.LENGTH_LONG).show();
                                        int balance = Integer.parseInt(mTopup_amount.getText().toString());
                                        model.setTokens(balance);
                                        UpdateUserBalance(balance);
                                    }
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Token To Cash", Toast.LENGTH_LONG).show();

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

    public void UpdateUserBalance(int balance){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

        double current_balance = model.getTokens();
        double final_balance = current_balance + balance;

        userRef.child("tokens").setValue(final_balance);
        mAccount_balance.setText(String.valueOf(final_balance));
    }
}
