package com.him.note_him_android;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class ViewNote extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    Button play;
    TextView title, description;
    ImageView imageView;

    String audioLink, imageLink, tile, desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        play = findViewById(R.id.play);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        imageView = findViewById(R.id.imageView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            audioLink = bundle.getString("audio");
            imageLink = bundle.getString("image");
            tile = bundle.getString("title");
            desc = bundle.getString("desc");


        }
        title.setText(tile);
        description.setText(desc);
        if (TextUtils.isEmpty(audioLink)) {
            play.setVisibility(View.GONE);
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioLink);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
            }
        });


        File file = new File(imageLink);
        Uri uri = Uri.fromFile(file);
        imageView.setImageURI(uri);
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        super.onStop();
    }

