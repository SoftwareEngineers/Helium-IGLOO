package helium.com.igloo.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1.LongRunningRecognizeRequest;
import com.google.cloud.speech.v1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.speech.v1.WordInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.protobuf.ByteString;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.UserModel;
import helium.com.igloo.R;
import helium.com.igloo.ViewArchiveActivity;
import helium.com.igloo.ViewLectureActivity;

import static java.lang.Thread.sleep;

public class PendingLectureAdapter extends RecyclerView.Adapter<PendingLectureAdapter.PendingLectureViewHolder> {
    private List<LectureModel> lectures;
    private Context context;
    private FirebaseAuth auth;
    private UserModel user;
    private ProgressDialog DownloadDialog;
    private ProgressDialog AudioExtractiondialog;
    private ProgressDialog TranscribeDialog;
    private File sdCard;
    private FFmpeg ffmpeg;
    private String transcribedText;
    private String lectureID;
    private SpeechClient client;

    public PendingLectureAdapter(List<LectureModel> lectures, Context context, FFmpeg ffmpeg) {
        this.lectures = lectures;
        this.context = context;
        auth = FirebaseAuth.getInstance();


        DownloadDialog = new ProgressDialog(context);
        DownloadDialog.setMessage("Processing....");
        DownloadDialog.setIndeterminate(true);
        DownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DownloadDialog.setCancelable(false);

        TranscribeDialog = new ProgressDialog(context);
        TranscribeDialog.setTitle("Transcribing");
        TranscribeDialog.setMessage("Proccesing...");
        TranscribeDialog.setCancelable(false);

        //DIALOG FOR EXTRACTING AUDIO FROM VIDEO

        AudioExtractiondialog = new ProgressDialog(context);
        AudioExtractiondialog.setTitle(null);

        sdCard = Environment.getExternalStorageDirectory();
        this.ffmpeg = ffmpeg;
    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    @Override
    public void onBindViewHolder(final PendingLectureViewHolder lectureViewHolder, int i) {
        final LectureModel p = lectures.get(i);
        lectureViewHolder.textTitle.setText(p.getTitle());
        lectureViewHolder.textViews.setText(String.valueOf(p.getViews()));

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef1 = storage.getReferenceFromUrl("gs://helium-igloo0830.appspot.com/images/").child(p.getThumbnail());
        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef1.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                lectureViewHolder.img_lecture.setImageBitmap(bitmap);
            }
        });



        lectureViewHolder.buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lectureID = p.getId();
                InitializeTranscripts(p.getArchive_id());
            }
        });

        lectureViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewArchiveActivity.class);
                intent.putExtra("archiveID", p.getArchive_id());
                intent.putExtra("key", p.getId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public PendingLectureAdapter.PendingLectureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_pending_lectures, viewGroup, false);
        return new PendingLectureViewHolder(itemView);
    }


    public class PendingLectureViewHolder extends RecyclerView.ViewHolder{

        protected ImageView img_lecture;
        protected TextView textTitle;
        protected TextView textViews;
        protected Button buttonUpload;
        protected View view;

         PendingLectureViewHolder(View v) {
            super(v);
            view = v;
            img_lecture  = (ImageView)v.findViewById(R.id.img_pending_lecture);
            textTitle = (TextView) v.findViewById(R.id.txt_pending_title);
            textViews = (TextView) v.findViewById(R.id.txt_pending_views);
            buttonUpload  = (Button) v.findViewById(R.id.btn_pending_upload);
        }
    }




    /////// INITIALIZE

    private void InitializeTranscripts(String archiveID){


        RequestQueue reqQueue = Volley.newRequestQueue(context);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/" + archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String url;
                    url= response.getString("url");
                    //Toast.makeText(context,url,Toast.LENGTH_LONG).show();
                    Log.d("Sample",url);

                    DownloadVideoFromWeb(url);

                    Log.e("Opentok Archive", "Archive starting");

                } catch (Exception e) {
                    Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

    }





    ///// AUDIO EXTRACTION

    private void ExtractAudio(File video) {
        try {
            File audio = new File(sdCard.getAbsolutePath(), "Iglo/audio.flac");
            if (audio.exists()) {
                audio.getCanonicalFile().delete();
            }
            if(video.exists()){
                String command ="-i "+ String.valueOf(video) +" -c:a flac "+String.valueOf(audio);
                executeFFmpeg(command.split(" "));
            }
        }catch (Exception e){
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }

    }

    private void executeFFmpeg(final String[] cmd)  {

        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d("Sample text", "Started: " + cmd);
                    AudioExtractiondialog.setMessage("Processing...");
                    AudioExtractiondialog.setTitle("Extracting Audio");
                    AudioExtractiondialog.setCancelable(false);
                    AudioExtractiondialog.show();
                }

                @Override
                public void onProgress(String message) {
                    AudioExtractiondialog.setMessage("Processing\n"+message);
                    Log.d("Sample Text", "Progress "+ message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("Sample Text", "Failed: "+ message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d("Sample Text", "Success: "+ message);
                }

                @Override
                public void onFinish() {
                    AudioExtractiondialog.dismiss();
                    TranscribeDialog.show();
                    Toast.makeText(context, "Extraction Success : ", Toast.LENGTH_LONG).show();
                    Recognize();

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }







    //// DOWNLOAD FILE FROM WEB

    private void DownloadVideoFromWeb(String url){

        final DownloadTask downloadTask = new DownloadTask(context);
        downloadTask.execute(url);

        DownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                File folder = new File(sdCard.getAbsolutePath(), "Iglo");
                if(!folder.exists()){
                    folder.mkdir();
                }

                File file = new File(sdCard.getAbsolutePath(),"Iglo/video.mp4");
                if(file.exists()){
                    file.getCanonicalFile().delete();
                }
                input = connection.getInputStream();
                output = new FileOutputStream(sdCard.getAbsolutePath() + "/Iglo/video.mp4");


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
            }
            mWakeLock.acquire();
            DownloadDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            DownloadDialog.setIndeterminate(false);
            DownloadDialog.setMax(100);
            DownloadDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            DownloadDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else {
                File video = new File(sdCard.getAbsolutePath(),"Iglo/video.mp4");
                Toast.makeText(context, "Download success: ", Toast.LENGTH_LONG).show();
                ExtractAudio(video);

            }
        }
    }







    /////RECOGNIZE AUDIO

    private void Recognize(){
        transcribedText = "";
        try {
            File audio = new File(sdCard.getAbsolutePath()+"/Iglo/audio.flac");
            try{
                CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(context.getResources().openRawResource(R.raw.credentials)));
                SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
                client = SpeechClient.create(settings);
                LongRunningRecognizeRequest request = LongRunningRecognizeRequest.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                                .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                                .setLanguageCode("en-US")
                                .build())
                        .setAudio(RecognitionAudio.newBuilder()
                                .setContent(ByteString.readFrom(new FileInputStream(audio)))
                                .build())
                        .build();
                // Use non-blocking call for getting file transcription
                OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                        client.longRunningRecognizeAsync(request);
                while (!response.isDone()) {
                    System.out.println("Waiting for response...");
                    Thread.sleep(1000);
                }
                List<SpeechRecognitionResult> results = response.get().getResultsList();


                for (SpeechRecognitionResult result : results) {
                    // There can be several alternative transcripts for a given chunk of speech. Just use the
                    // first (most likely) one here.
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    for (WordInfo wordInfo : alternative.getWordsList()) {
                            transcribedText += wordInfo.getWord()+" "+(wordInfo.getStartTime().getSeconds()*1000)+" ";
                    }
                }
                System.out.println("RESPONSE: "+transcribedText);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lectures").child(lectureID);

                reference.child("available").setValue(true);
                reference.child("transcription").setValue(transcribedText.trim());
                reference.child("is_transcribed").setValue(true);
                TranscribeDialog.hide();
                Toast.makeText(context,"transcribe", Toast.LENGTH_LONG).show();
                audio.getCanonicalFile().delete();
                File video = new File(sdCard.getAbsolutePath(),"Iglo/video.mp4");
                if (video.exists()){
                    try {
                        video.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

}
