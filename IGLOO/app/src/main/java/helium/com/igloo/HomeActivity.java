package helium.com.igloo;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Adapters.LectureSearchAdapter;
import helium.com.igloo.Fragments.HomeFragment;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private CircleImageView mTabPic;
    private DrawerLayout mDrawer;
    private TextView mName;
    private TextView mTokens;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private ImageButton mCreateLecture;

    private LectureSearchAdapter adapter;
    private LinearLayoutManager layoutManager;

    private static final String[] SUGGESTIONS = {
            "Bauru", "Sao Paulo", "Rio de Janeiro",
            "Bahia", "Mato Grosso", "Minas Gerais",
            "Tocantins", "Rio Grande do Sul"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ///adapter = new LectureSearchAdapter(HomeActivity.this,getDummyList());
        layoutManager = new LinearLayoutManager(this);


        View headerLayout = navigationView.getHeaderView(0);
        mTabPic = (CircleImageView)headerLayout.findViewById(R.id.tab_profile_pic);
        mName = (TextView)headerLayout.findViewById(R.id.tab_profile_name);
        mTokens = (TextView)headerLayout.findViewById(R.id.tab_profile_token);
        mCreateLecture = (ImageButton)headerLayout.findViewById(R.id.imgbtn_create_lecture);
        mCreateLecture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.choose_lecture_type_layout, null);
                Button buttonPublicLecture = (Button)mView.findViewById(R.id.btn_public_lecture);
                buttonPublicLecture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, CreatePublicLectureActivity.class);
                        startActivity(intent);
                    }
                });
                Button buttonPrivateLecture = (Button)mView.findViewById(R.id.btn_private_lecture);
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

                    startActivity(new Intent(HomeActivity.this, LandingActivity.class));
                    finish();
                }
                else{
                    setProfileInfo();
                }
            }
        };

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, new HomeFragment()).commit();
        setTitle("IGLOO");
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, new HomeFragment()).commit();

            setProfileInfo();
        }
        else if (id == R.id.menu_search){

            SearchManager searchManager = (SearchManager) HomeActivity.this.getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) item.getActionView();


            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(HomeActivity.this.getComponentName()));
                searchView.setIconified(false);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Toast.makeText(getApplicationContext(),query,Toast.LENGTH_LONG).show();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        GetSuggestions(newText);
                        return false;
                    }
                });

            }

        }


        return super.onOptionsItemSelected(item);
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
//            startActivity(new Intent(this, NotebookFragmentActivity.class));
//            finish();
        }
        else if (id == R.id.subscriptions) {
//            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.frame_container, new AchievementFragment()).commit();
//            setTitle("My Achievements");
        }
//        else if (id == R.id.settings) {
//            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.frame_container, new SettingsFragment()).commit();
//            setTitle("Settings");
//        }
//        else if (id == R.id.about) {
//            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.frame_container, new AboutUsFragment()).commit();
//            setTitle("About Us");
//        }
//
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



    public List<String> getDummyList()
    {
        List<String> list = new ArrayList<>();
        list.add("rohit");
        list.add("Amanda");
        list.add("Triple H");
        list.add("Barack Obama");
        list.add("Kesha");
        list.add("Ganguly");
        list.add("Tomatino");
        list.add("rohit");
        list.add("Amanda");
        list.add("Triple H");
        list.add("Barack Obama");
        list.add("Kesha");
        list.add("Ganguly");
        list.add("Tomatino");
        list.add("rohit");
        list.add("Amanda");
        list.add("Triple H");
        list.add("Barack Obama");
        list.add("Kesha");
        list.add("Ganguly");
        list.add("Tomatino");

        return list;
    }



    private void GetSuggestions(String query) {

        // Cursor
        Object[] temp;
        String current;
        final MatrixCursor cursor = new MatrixCursor(new String[] {  BaseColumns._ID, "LectureTitles"});
        StringBuilder output = new StringBuilder();
        for(int i = 0; i < SUGGESTIONS.length;i++) {
            current = SUGGESTIONS[i];
            //if(current.toLowerCase().startsWith(query.toLowerCase())) {
            if(current.toLowerCase().contains(query.toLowerCase())){
                output.append(current);
                output.append("\n");
                temp = new Object[]{i, current};
                cursor.addRow(temp);
            }

        }

        adapter = new LectureSearchAdapter(HomeActivity.this, cursor, SUGGESTIONS);
        //search.setSuggestionsAdapter(adapter);
        Toast.makeText(this,output.toString(),Toast.LENGTH_LONG).show();

    }


}
