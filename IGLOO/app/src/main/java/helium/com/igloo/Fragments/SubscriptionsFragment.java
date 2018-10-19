package helium.com.igloo.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.SubscriptionAdapter;
import helium.com.igloo.Models.SubscriptionModel;
import helium.com.igloo.R;

public class SubscriptionsFragment extends Fragment {

    private RecyclerView mSubscriptionList;
    private List<SubscriptionModel> mSubscriptions;
    private SubscriptionAdapter mSubscriptionAdapter;

    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        auth = FirebaseAuth.getInstance();

        mSubscriptionList = (RecyclerView) v.findViewById(R.id.subscription_list);
        mSubscriptions = new ArrayList<>();
        int numberOfColumns = 2;

        mSubscriptionList.setLayoutManager(new GridLayoutManager(SubscriptionsFragment.super.getContext(), numberOfColumns));
        mSubscriptionAdapter = new SubscriptionAdapter(mSubscriptions , SubscriptionsFragment.super.getContext());
        mSubscriptionList.setAdapter(mSubscriptionAdapter);

        loadSubscriptions();

        return v;
    }

    private void loadSubscriptions() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Subscriptions");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    SubscriptionModel subscriptions = new SubscriptionModel();
                    Log.e("dfdfdffdfdfdfdfdfdf",childSnapshot.getKey());
                    if (childSnapshot.getKey().equals(auth.getCurrentUser().getUid())) {
                        subscriptions = childSnapshot.getValue(SubscriptionModel.class);
                    }

                    mSubscriptions.add(subscriptions);
                    mSubscriptionAdapter.notifyDataSetChanged();
                    mSubscriptionList.smoothScrollToPosition(mSubscriptionAdapter.getItemCount());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
