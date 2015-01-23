package com.str2.ta_02studentattendancecheckerv2;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class ViewActivity extends ActionBarActivity {
    final String TAG = "ViewActivity";

    ListView logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        logList = (ListView) findViewById(R.id.logList);

        //read txt file
        String actualText = "";
        ArrayList<String> logListItems = new ArrayList<>();

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

            actualText = new String(finalByteArray, "UTF-8");
            Log.i(TAG, Arrays.toString(finalByteArray));
            Log.i(TAG, "Text in file: " + actualText);
        } catch (FileNotFoundException fnfe){
            Log.i(TAG, fnfe.toString());
        } catch (IOException ioe){
            Log.i(TAG, ioe.toString());
        }

        ArrayList<String> lines = new ArrayList<>();
        int previousIndex = 0;
        for(int i = actualText.indexOf("]"); i < actualText.length(); i++){
            if((actualText.charAt(i)+"").equals("]")){
                lines.add(actualText.substring(previousIndex, i+1));
                previousIndex = i+1;
            }
        }

        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String subject = line.substring(0, line.indexOf("_"));
            String period = line.substring(line.indexOf("_") + 1, line.indexOf("_", line.indexOf("_") + 1));
            String classlist = line.substring(line.indexOf("["), line.indexOf("]"));

            int numberTardy = 0;
            int numberAbsent = 0;

            int cn = 0;
            for (int j = 0; j < classlist.length(); j++) {
                if ((classlist.charAt(j) + "").equals("0") || (classlist.charAt(j) + "").equals("1") || (classlist.charAt(j) + "").equals("2")) {
                    cn++;
                    int pta = Integer.parseInt(classlist.charAt(j) + "");
                    switch (pta) {
                        case 0:
                            Log.i(TAG, "CN" + ((cn < 10) ? ("0" + cn) : (cn)) + "PRESENT");
                            break;
                        case 1:
                            Log.i(TAG, "CN" + ((cn < 10) ? ("0" + cn) : (cn)) + "TARDY");
                            numberTardy++;
                            break;
                        case 2:
                            Log.i(TAG, "CN" + ((cn < 10) ? ("0" + cn) : (cn)) + "ABSENT");
                            numberAbsent++;
                            break;
                    }
                }
            }

            logListItems.add(new String().concat(subject + " " + period + " Tardy: " + numberTardy + "; Absent: " + numberAbsent));
        }

        Log.i(TAG, "Items: " + logListItems.size());
        logList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logListItems));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
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
}
