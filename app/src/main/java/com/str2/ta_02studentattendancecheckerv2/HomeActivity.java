package com.str2.ta_02studentattendancecheckerv2;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class HomeActivity extends ActionBarActivity {
    String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLogButtonClick(View view){
        Intent logIntent = new Intent(getApplicationContext(), LogActivity.class);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
                        // add all of DetailsActivity's parents to the stack,
                        // followed by DetailsActivity itself
                        .addNextIntentWithParentStack(logIntent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendingIntent);
        try {
            pendingIntent.send();
        } catch(PendingIntent.CanceledException ce){
            Log.i(TAG, ce.toString());
        }
        finish();
    }

    public void onSendButtonClick(View view){
        File f = new File(getFilesDir().getAbsolutePath() + "/" + LogActivity.OUTPUTFILENAME);
        if(f.exists()){
            Intent sendIntent = new Intent(getApplicationContext(), SendDataActivity.class);
            PendingIntent pendingIntent =
                    TaskStackBuilder.create(this)
                            // add all of DetailsActivity's parents to the stack,
                            // followed by DetailsActivity itself
                            .addNextIntentWithParentStack(sendIntent)
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(pendingIntent);
            try {
                pendingIntent.send();
            } catch(PendingIntent.CanceledException ce){
                Log.i(TAG, ce.toString());
            }
            finish();
        } else {
            Log.i(TAG, "No file");
            Toast.makeText(getApplicationContext(), "No file found.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onViewButtonClick(View view){
            File f = new File(getFilesDir().getAbsolutePath() + "/" + LogActivity.OUTPUTFILENAME);
            if(f.exists()){
                Intent viewIntent = new Intent(getApplicationContext(), ViewActivity.class);
                PendingIntent pendingIntent =
                        TaskStackBuilder.create(this)
                                // add all of DetailsActivity's parents to the stack,
                                // followed by DetailsActivity itself
                                .addNextIntentWithParentStack(viewIntent)
                                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentIntent(pendingIntent);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException ce) {
                    Log.i(TAG, ce.toString());
                }
                finish();
            } else {
                Log.i(TAG, "No file");
                Toast.makeText(getApplicationContext(), "No file found.", Toast.LENGTH_SHORT).show();
            }
    }

    public void onResetClick(View view){
        deleteFile(LogActivity.OUTPUTFILENAME);
        Toast.makeText(this, "Local data deleted.", Toast.LENGTH_SHORT).show();
    }
}
