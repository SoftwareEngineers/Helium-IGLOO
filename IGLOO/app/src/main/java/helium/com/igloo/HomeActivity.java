package helium.com.igloo;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.media.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Adapters.LectureSearchAdapter;
import helium.com.igloo.Fragments.HomeFragment;
import helium.com.igloo.Fragments.SubscriptionsFragment;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.NotificationModel;
import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.Models.SubscriptionModel;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private CircleImageView mTabPic;
    private DrawerLayout mDrawer;
    private TextView mName;
    private TextView mTokens;
    private Button mLogout;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private ImageButton mCreateLecture;
    private SearchView searchView;
    private SearchView.SearchAutoComplete   mSearchAutoComplete;
    private ArrayAdapter<String> adapter;
    private List<LectureModel> lectures;
    private List<String> titles;

    private boolean doubleBackToExitPressedOnce = false;
    private DatabaseReference databaseReference;
    private List<SubscriptionModel> subscriptionModelList;
    private SubscriptionModel subscriptionModel;
    private List<NotificationModel> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemBackgroundResource(R.drawable.item_highlight);

        titles = new ArrayList<>(); ////// DATA FOR DISLPAYING SEARCH RESULTS
        lectures = new ArrayList<>(); ////// DATABASE DATA
        notifications = new ArrayList<>();

        getTitlesAndIDs();

        View headerLayout = navigationView.getHeaderView(0);
        mTabPic = (CircleImageView) headerLayout.findViewById(R.id.tab_profile_pic);
        mName = (TextView) headerLayout.findViewById(R.id.tab_profile_name);
        mTokens = (TextView) headerLayout.findViewById(R.id.tab_profile_token);
        mLogout = (Button) navigationView.findViewById(R.id.logout_button);
        mProgressDialog = new ProgressDialog(this);
        mCreateLecture = (ImageButton) headerLayout.findViewById(R.id.imgbtn_create_lecture);

        mCreateLecture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.choose_lecture_type_layout, null);

                Button buttonPublicLecture = (Button) mView.findViewById(R.id.btn_public_lecture);
                Button buttonPrivateLecture = (Button) mView.findViewById(R.id.btn_private_lecture);

                buttonPublicLecture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, CreatePublicLectureActivity.class);
                        startActivity(intent);
                    }
                });

                buttonPrivateLecture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, CreatePrivateLectureActivity.class);
                        startActivity(intent);
                    }
                });

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });


        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    mProgressDialog.setMessage("Signing out....");
                    mProgressDialog.show();
                    CountDown cd = new CountDown(500, 100);
                    cd.start();
                } else {
                    setProfileInfo();
                }
            }
        };

        subscriptionModelList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Subscription").child(auth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subscriptionModelList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    subscriptionModel = childSnapshot.getValue(SubscriptionModel.class);
                    if(subscriptionModel.getStatus().equals("pending")){
                        subscriptionModelList.add(subscriptionModel);
                    }
                }
                for(int i=0;i<subscriptionModelList.size();i++){
                    Notification noti = new Notification.Builder(HomeActivity.this)
                            .setContentTitle(subscriptionModelList.get(i).getSubscriber()+" has subscribed to you")
                            .setContentText("")
                            .setSmallIcon(R.drawable.ic_coin)
                            .build();
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    noti.flags |= Notification.FLAG_AUTO_CANCEL;
                    nm.notify(0,noti);
                    NotificationModel n = new NotificationModel();
                    n.setNotification_title(subscriptionModelList.get(i).getSubscriber()+" has subscribed to you");
                    n.setNotification_description("");
                    notifications.add(n);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimary));
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, new HomeFragment()).commit();
        setTitle("IGLOO");

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();

                editor.remove("email");
                editor.remove("password");
                editor.commit();

                auth.signOut();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tool, menu);
        MenuItem item = menu.findItem(R.id.menu_search);

        searchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchAutoComplete =  searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        //mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.background_white);
        //mSearchAutoComplete.setBackgroundColor(Color.WHITE);
        mSearchAutoComplete.setDropDownAnchor(R.id.menu_search);
        mSearchAutoComplete.setThreshold(1);

        adapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_list_item_1,titles);
        mSearchAutoComplete.setAdapter(adapter);
        mSearchAutoComplete.showDropDown();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_notification) {
            Intent i = new Intent(HomeActivity.this,NotificationActivity.class);
            i.putExtra("notifications",(Serializable) notifications);
            startActivity(i);
            overridePendingTransition(R.transition.slide_in_right,R.transition.slide_out_left);
        }
        else if (id == R.id.menu_search){

            mSearchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = (String) parent.getItemAtPosition(position);
                    int index = titles.indexOf(item);
                    LectureModel model = lectures.get(index);

                    Intent intent;
                    if(model.getAvailable() && !model.getLive()) {
                        intent = new Intent(HomeActivity.this, ViewArchiveActivity.class);
                        intent.putExtra("key", model.getId());
                        intent.putExtra("archiveID", model.getArchive_id());
                        startActivity(intent);

                    }
                    else if (!model.getAvailable() && model.getLive()){
                        intent = new Intent(HomeActivity.this, ViewLectureActivity.class);
                        intent.putExtra("key", model.getId());
                        startActivity(intent);
                    }
                }
            });
            }



        return super.onOptionsItemSelected(item);
    }

    public void getTitlesAndIDs(){

        final LectureModel[] model = new LectureModel[1];
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lectures");
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 lectures.clear();
                 titles.clear();
                 for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                     //Toast.makeText(HomeActivity.this,childSnapshot.child("lecture").getValue(String.class), Toast.LENGTH_SHORT).show();
                     model[0] = childSnapshot.getValue(LectureModel.class);
                     lectures.add(model[0]);
                     titles.add(model[0].getTitle());
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void setProfileInfo(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(auth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String pName = dataSnapshot.child("name").getValue(String.class);
                int pTokens = dataSnapshot.child("tokens").getValue(Integer.class);
                String url = dataSnapshot.child("profileUrl").getValue(String.class);

                storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://igloo-0830.appspot.com/images/").child(url);
                final long ONE_MEGABYTE = 1024 * 1024;
                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mTabPic.setImageBitmap(bitmap);
                    }
                });

                mName.setText(pName);
                mTokens.setText(Integer.toString(pTokens));

                mTabPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, new HomeFragment()).commit();
        }
        else if (id == R.id.subscriptions) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, new SubscriptionsFragment()).commit();
        }
        else if (id == R.id.payment) {
            startActivity(new Intent(HomeActivity.this, PaymentActivity.class));
        }
        else {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
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
            startActivity(new Intent(HomeActivity.this, SigningInActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            super.onBackPressed();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
