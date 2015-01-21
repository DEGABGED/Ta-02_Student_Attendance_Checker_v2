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
        Intent i = new Intent(getApplicationContext(), LogActivity.class);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
                        // add all of DetailsActivity's parents to the stack,
                        // followed by DetailsActivity itself
                        .addNextIntentWithParentStack(i)
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
        Intent i = new Intent(getApplicationContext(), SendDataActivity.class);
        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
                        // add all of DetailsActivity's parents to the stack,
                        // followed by DetailsActivity itself
                        .addNextIntentWithParentStack(i)
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

    public void onViewButtonClick(View view){
        //block of code to read a file
        try{
            FileInputStream fis = openFileInput(LogActivity.OUTPUTFILENAME);
            ArrayList<Byte> bytearraylist = new ArrayList<Byte>();
            int readInt = 0;
            readInt = fis.read();
            while (readInt >= 0) {
                bytearraylist.add(Byte.valueOf((byte) readInt));
                readInt = fis.read();
            }

            byte[] finalByteArray = new byte[bytearraylist.size()];
            for(int i = 0; i < bytearraylist.size(); ++i){
                finalByteArray[i] = bytearraylist.get(i);
            }

            String actualText = new String(finalByteArray, "UTF-8");
            Log.i(TAG, Arrays.toString(finalByteArray));
            Log.i(TAG, "Text in file: " + actualText);
        } catch (FileNotFoundException fnfe){
            Log.i(TAG, fnfe.toString());
        } catch (IOException ioe){
            Log.i(TAG, ioe.toString());
        }
    }

    public void onResetClick(View view){
        deleteFile(LogActivity.OUTPUTFILENAME);
        Toast.makeText(this, "Local data deleted.", Toast.LENGTH_SHORT).show();
    }
}
