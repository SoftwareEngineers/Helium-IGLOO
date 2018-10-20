package helium.com.igloo.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import helium.com.igloo.Models.TranscriptionModel;
import helium.com.igloo.R;

public class TransciptionAdapter extends ArrayAdapter<TranscriptionModel>{
    private List<TranscriptionModel> transcriptionModelList;

    public TransciptionAdapter(@NonNull Context context, @NonNull List<TranscriptionModel> transcriptList) {
        super(context,0, transcriptList);
        transcriptionModelList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.transcription_layout, parent,false);
        }
        TextView txtWord = convertView.findViewById(R.id.txtTransciptionWord);
        TextView txtTime = convertView.findViewById(R.id.txtTransciptionTime);
        TranscriptionModel trans = getItem(position);
        if(trans != null){
            txtWord.setText(trans.getWord());
            txtTime.setText(trans.getTime()+"");
        }
        return convertView;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<TranscriptionModel> suggestions = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                suggestions.addAll(transcriptionModelList);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(TranscriptionModel trans : transcriptionModelList){
                    if(trans.getWord().toLowerCase().trim().contains(filterPattern)){
                        suggestions.add(trans);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((TranscriptionModel) resultValue).getWord();
        }
    };
}
