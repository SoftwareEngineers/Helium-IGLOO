package helium.com.igloo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.MediaController;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import helium.com.igloo.Adapters.QuestionAdapter;
import helium.com.igloo.Models.LectureModel;
import helium.com.igloo.Models.QuestionModel;
import helium.com.igloo.Models.UserModel;
import helium.com.igloo.SpeechRecognition.SpeechService;


public class ViewArchiveActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private ProgressBar progressBar;
    private Uri mUrl;
    private RecyclerView mRecycleViewQuestions;
    private List<QuestionModel> questions;
    private QuestionAdapter questionAdapter;
    private String mKey,archiveID;
    private FFmpeg ffmpeg;
    private ProgressDialog AudioExtractiondialog,DownloadDialog;
    private File sdCard;
    private LectureModel lecture;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    //recognized text
                    //Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                    lecture.setTranscription(text);
                    lecture.setIs_transcribed(true);
                    updateLecture();
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {

            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            Toast.makeText(getApplicationContext(),"Service Connected",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {


        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_archive);

        //DIALOG FOR DOWNLOADING VIEO FROM SERVER

        DownloadDialog = new ProgressDialog(ViewArchiveActivity.this);
        DownloadDialog.setMessage("Processing....");
        DownloadDialog.setIndeterminate(true);
        DownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        DownloadDialog.setCancelable(false);

        //DIALOG FOR EXTRACTING AUDIO FROM VIDEO

        AudioExtractiondialog = new ProgressDialog(this);
        AudioExtractiondialog.setTitle(null);


        sdCard = Environment.getExternalStorageDirectory();
        loadFFmpeg();


        videoView = findViewById(R.id.view_lecture);
        progressBar = findViewById(R.id.prog_archive);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        archiveID = intent.getStringExtra("archiveID");
        mKey = intent.getStringExtra("key");


        mRecycleViewQuestions = (RecyclerView) findViewById(R.id.rec_questions);
        questions = new ArrayList<>();
        questionAdapter = new QuestionAdapter(questions, this, videoView);
        mRecycleViewQuestions.setAdapter(questionAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleViewQuestions.setLayoutManager(layoutManager);
        loadQuestions();
        LoadLecture();
        if (lecture.getIs_transcribed()) {
            playArchive(archiveID);
        } else {
            InitializeTranscripts();
        }
    }


    public void playArchive(String archiveID){
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/" + archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    mediaController = new MediaController(ViewArchiveActivity.this);
                    mediaController.setAnchorView(videoView);
                    Uri video = Uri.parse(response.getString("url"));

                    Log.e("Opentok Archive", "Archive starting");
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(video);

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        public void onPrepared(MediaPlayer mp) {
                            questionAdapter.getMediaPlayer(mp);
                            progressBar.setVisibility(View.GONE);
                            //videoView.start();
                            Log.e("Opentok Archive", "Archive started");
                        }
                    });

                } catch (Exception e) {
                    System.out.println("Video Play Error :" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewArchiveActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void loadQuestions(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Questions");
        databaseReference.orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questions.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if(childSnapshot.child("lecture").getValue(String.class).equals(mKey) && childSnapshot.child("is_answered").getValue(Boolean.class)){
                        QuestionModel question = childSnapshot.getValue(QuestionModel.class);
                        questions.add(question);
                        questionAdapter.notifyDataSetChanged();
                        mRecycleViewQuestions.smoothScrollToPosition(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void LoadLecture(){
        Toast.makeText(getApplicationContext(),"im here",Toast.LENGTH_LONG).show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lectures").child(mKey);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lecture = dataSnapshot.getValue(LectureModel.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void InitializeTranscripts(){


        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com/videos/" + archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String url;
                    url= response.getString("url");
                    //Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
                    Log.d("Sample",url);

                    DownloadVideoFromWeb(url);

                    mediaController = new MediaController(ViewArchiveActivity.this);
                    mediaController.setAnchorView(videoView);
                    Uri video = Uri.parse(url);

                    Log.e("Opentok Archive", "Archive starting");
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(video);

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        public void onPrepared(MediaPlayer mp) {
                            questionAdapter.getMediaPlayer(mp);
                            progressBar.setVisibility(View.GONE);
                            videoView.start();
                            Log.e("Opentok Archive", "Archive started");
                        }
                    });


                } catch (Exception e) {
                    Toast.makeText(ViewArchiveActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewArchiveActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));





    }



    /////// EXTRACTING AUDIO FROM VIDEO
    @Override
    public void onStart(){
        super.onStart();
        if(this.bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE)){

        }else{

        }
    }
    @Override
    public void onStop(){
        super.onStop();
        unbindService(mServiceConnection);
    }
    private void ExtractAudio(File video) {
        try {
            File audio = new File(sdCard.getAbsolutePath(), "Iglo/audio.flac");

            if (audio.exists()) {
                audio.getCanonicalFile().delete();
            }
            if(video.exists()){
                String command ="-i "+ video +" -c:a flac "+String.valueOf(audio);
                executeFFmpeg(command.split(" "));
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }

    }

    private void loadFFmpeg() {
        ffmpeg = FFmpeg.getInstance(ViewArchiveActivity.this.getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {

                }
                @Override
                public void onFinish() {
                    //lblView.setText("Finished loading library!");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
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
                    videoView.start();
                    Recognize();

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }





    /////// DOWNLOADING VIDEO FROM SERVER

    private void DownloadVideoFromWeb(String url){

        final DownloadTask downloadTask = new DownloadTask(ViewArchiveActivity.this);
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
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                File video = new File(sdCard.getAbsolutePath(),"Iglo/video.mp4");
                ExtractAudio(video);
            }
        }
    }




    /////// SPEECH RECOGNITION

    private void Recognize(){

        try {
            File audio = new File(sdCard.getAbsolutePath()+ "/Iglo/audio.flac");
            if (mSpeechService != null) {
                FileInputStream fis = new FileInputStream(audio);
                mSpeechService.recognizeInputStream(fis);
                Toast.makeText(getApplicationContext(),"start recognize",Toast.LENGTH_LONG).show();
            } else {

            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }


    private void updateLecture(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lectures").child(mKey);
        reference.child("transcription").setValue(lecture.getTranscription());
        reference.child("is_transcribed").setValue(lecture.getIs_transcribed());

    }

}
