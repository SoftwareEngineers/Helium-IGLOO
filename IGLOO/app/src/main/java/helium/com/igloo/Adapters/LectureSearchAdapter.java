package helium.com.igloo.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        text.setTextAppearance(R.style.searchAppearance);
        text.setText(items[cursor.getPosition()]);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_lecture_search, parent, false);
        text =  view.findViewById(R.id.fragment_lecture_titles);

        return view;

    }

}
