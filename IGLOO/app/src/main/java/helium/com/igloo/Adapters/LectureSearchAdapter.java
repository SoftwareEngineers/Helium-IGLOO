package helium.com.igloo.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.R;

public class LectureSearchAdapter extends CursorAdapter {

    private String[] items;
    private TextView text;
    private Cursor list_cursor;

    public LectureSearchAdapter(Context context, Cursor cursor, String[] items) {
        super(context, cursor, false);
        this.items = items;
        this.list_cursor = cursor;
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        text.setText(items[cursor.getPosition()]);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_lecture_search, parent, false);
        text =  view.findViewById(R.id.fragment_lecture_titles);
        //bindView(view,context,cursor);

        return view;

    }

}
