/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helium.com.igloo.SpeechRecognition;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import helium.com.igloo.R;


public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    // View references
    private TextView mText;
    //private ResultAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button btnStart,btnConvert;
    private MediaRecorder mediaRecorder;
    private boolean started = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            Toast.makeText(MainActivity.this,"startedServiceConnection",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            Toast.makeText(MainActivity.this,"serviceDisconnected",Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        setContentView(R.layout.activity_temporary_speech_recognition);
        mText = findViewById(R.id.txtTemp);
        mRecyclerView =  findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        mAdapter = new ResultAdapter(results);
        mRecyclerView.setAdapter(mAdapter);

        btnStart = findViewById(R.id.btnStart);
        btnConvert = findViewById(R.id.btnConvert);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!started) {
                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        btnStart.setText("stop");
                        started = true;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    mediaRecorder.stop();
                    started = false;
                    btnStart.setText("start");
                    setupMediaRecorder();
                }
            }
        });
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSpeechService !=null) {
                    //mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
                    try {
                        File file = new File(Environment.getExternalStorageDirectory()+"/record.3pg");
                        FlacEncoder encoder = new FlacEncoder();
                        File file2 = new File(Environment.getExternalStorageDirectory()+"/flacoldresult.flac");
                        if(encoder.encode(file,file2) == FlacEncoder.Status.FULL_ENCODE){
                            FileInputStream fis = new FileInputStream(file2);
                            mSpeechService.recognizeInputStream(fis);
                            Toast.makeText(MainActivity.this,"encoded",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this,"error encoding",Toast.LENGTH_SHORT).show();
                        }



                    }catch(Exception e){
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"null mspeechservice",Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }
    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_WB);
        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory()+"/record.3pg");
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API

        if(this.bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE)){

        }else{
            Toast.makeText(MainActivity.this,"failed to start service",Toast.LENGTH_SHORT).show();
        }


        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        // Stop Cloud Speech API
        unbindService(mServiceConnection);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*if (mAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
        }*/
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    //mAdapter.addResult(text);
                                    mRecyclerView.smoothScrollToPosition(0);
                                } else {
                                    mText.setText(text);
                                }
                            }
                        });
                    }else{
                        Toast.makeText(MainActivity.this,"null string",Toast.LENGTH_LONG).show();
                    }
                }
            };

   /* private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_result, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
        }

    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }*/
}
