package helium.com.igloo.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import helium.com.igloo.Models.TranscriptionModel;
import helium.com.igloo.R;

public class TransciptionAdapter extends RecyclerView.Adapter<TransciptionAdapter.TranscriptionViewHolder>{

    private List<TranscriptionModel> transcriptions;
    private Context context;

    public TransciptionAdapter(List<TranscriptionModel> transcriptions, Context context) {
        this.transcriptions = transcriptions;
        this.context = context;
    }

    @Override
    public TranscriptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(context).
                inflate(R.layout.transcription_layout, parent, false);
        return new TranscriptionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TranscriptionViewHolder holder, int position) {
        final TranscriptionModel trans = transcriptions.get(position);
        holder.transcriptionWord.setText(trans.getWord());
        holder.transcriptionTime.setText(trans.getTime()+"");
    }

    @Override
    public int getItemCount() {
        return transcriptions.size();
    }

    public class TranscriptionViewHolder extends RecyclerView.ViewHolder{
        protected TextView transcriptionWord;
        protected TextView transcriptionTime;
        public TranscriptionViewHolder(View itemView) {
            super(itemView);
            transcriptionWord = itemView.findViewById(R.id.txtTransciptionWord);
            transcriptionTime = itemView.findViewById(R.id.txtTransciptionTime);
        }
    }
}
