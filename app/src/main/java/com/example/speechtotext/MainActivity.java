package com.example.speechtotext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    public FloatingActionButton fab;
    private TextView et_content;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    Calendar today = Calendar.getInstance();
    private Context context;
    public ImageButton share,delete,saved;
    Toolbar toolbar;
    public DrawerLayout drawer;
    public boolean isClickedFirstTime = true;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.INTERNET
                    },
                    REQUEST_WRITE_STORAGE);
        }
        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        delete =  (ImageButton) findViewById(R.id.delete);
        share = (ImageButton) findViewById(R.id.share);
        saved = (ImageButton) findViewById(R.id.saved);
        et_content = (TextView) findViewById(R.id.txtSpeechInput);
        fab =(FloatingActionButton) findViewById(R.id.fab);

        et_content.setMovementMethod(new ScrollingMovementMethod());

        fab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#808000")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClickedFirstTime){
                    fab.setImageResource(R.drawable.ic_mic_black_24dp);
                    promptSpeechInput();


                }else {
                    fab.setImageResource(R.drawable.stop);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_content.setText("");
            }
        });


        actionBarDrawerToggle = new ActionBarDrawerToggle( this,drawer,toolbar,R.string.open,R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        navigationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupavigationview();
            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Here is the share content body";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = et_content.getText().toString();
                String arr []  = title.split(" ",2);
                String filename = arr[0];
                String content = et_content.getText().toString();

                if (!filename.equals("") && !content.equals("")) {
                    saveTextasFile(filename, content);
                }
            }
        });
    }

    private void setupavigationview(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.create_new:
                        et_content.setText("");
                        break;
                    case R.id.open:
                        Toast.makeText(MainActivity.this, "Open", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.delete:
                        et_content.setText("");
                        break;
                }
                return false;
            }
        });
    }


    private void saveTextasFile(String filename, String content){
        String fileName = filename +".txt";

        //create a file

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),fileName);

        //write a file

        try{
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this,"Saved!",Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this,"Fail not found!",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"Error saving file!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Showing google speech input dialog
     **/
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    et_content.setText(result.get(0));
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       //   getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}