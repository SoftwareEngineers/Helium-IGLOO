package helium.com.igloo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.opentok.android.Session;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewArchiveActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_archive);
        videoView = (VideoView)findViewById(R.id.view_lecture);
        progressBar = (ProgressBar)findViewById(R.id.prog_archive);
        Intent intent = getIntent();
        String archiveID = intent.getStringExtra("archiveID");
        Log.e("Opentok Archive", "Arvhie klskdlskld" + archiveID);
        try {
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse("https://s3.amazonaws.com/tokbox.com.archive2/46176272"+ archiveID + "/archive.mp4");
            Log.e("Opentok Archive", "Archive starting");
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    videoView.start();
                    Log.e("Opentok Archive", "Archive started");
                }
            });
        } catch (Exception e) {
            System.out.println("Video Play Error :" + e.getMessage());
        }
    }

    public String getUrl(String archiveID){
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        final String[] url = {""};
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://iglov2.herokuapp.com" + "/videos/" + archiveID,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    url[0] = response.getString("url");
                } catch (JSONException error) {
                    Toast.makeText(ViewArchiveActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewArchiveActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
        return url[0];
    }
}
