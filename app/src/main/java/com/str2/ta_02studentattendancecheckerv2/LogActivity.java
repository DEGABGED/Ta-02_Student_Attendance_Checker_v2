package com.str2.ta_02studentattendancecheckerv2;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class LogActivity extends ActionBarActivity implements ActionCompleteDialogFragment.ActionCompleteDialogListener{
    private static String TAG = "Log activity SAC";

    static ListView studentlist;
    static Spinner timeperiod;
    static EditText subject;
    static String subjecttext, periodtext;
    static int[] attendance;
    static ArrayList<String> students;
    static ArrayList<String> studentsBase;
    static String OUTPUTFILENAME = "Saved_Logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        students = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.class_list)));
        studentsBase = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.class_list)));
        attendance = new int[students.size()];

        //set up textedit for subject choosing
        subject = (EditText) findViewById(R.id.subjectname);

        //set up the spinner for class period choosing
        timeperiod = (Spinner) findViewById(R.id.timeperiod);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.time_periods,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeperiod.setAdapter(spinnerAdapter);
        timeperiod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodtext = parent.getItemAtPosition(position).toString();
                Log.i(TAG, "Selected thing: " + periodtext);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "Nothing was selected");
            }
        });

        //set up the listview for the student checking
        studentlist = (ListView) findViewById(R.id.studentlist);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, students);
        studentlist.setAdapter(listAdapter);
        studentlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Clicked person: " + parent.getItemAtPosition(position).toString());
                attendance[position]++;
                switch(attendance[position]){
                    case 1:
                        students.set(position, studentsBase.get(position)+" [TARDY]");
                        Log.i(TAG, parent.getItemAtPosition(position).toString() + "is tardy");
                        break;
                    case 2:
                        students.set(position, studentsBase.get(position)+" [ABSENT]");
                        Log.i(TAG, parent.getItemAtPosition(position).toString() + "is absent");
                        break;
                    case 3:
                        students.set(position, studentsBase.get(position));
                        attendance[position] = 0;
                        break;
                }
                ((BaseAdapter) studentlist.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log, menu);
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

    public void onStoreButtonClick(View view){
        subjecttext = subject.getText().toString();

        //For Formatting into String
        //TODO add Section Choice
        String finalOutput = "";
        //finalOutput.append(sectionChoice + " ");
        finalOutput = finalOutput.concat(subjecttext + "_" + periodtext + "_" + Arrays.toString(attendance));
        Log.i(TAG, finalOutput);

        try {
            ArrayList<Byte> bytearraylist = new ArrayList<>();

            try {
                //File Data Re-write
                FileInputStream fis = openFileInput(OUTPUTFILENAME);
                int readInt = 0;

                readInt = fis.read();
                while (readInt >= 0) {
                    bytearraylist.add(Byte.valueOf((byte) readInt));
                    readInt = fis.read();
                }

                fis.close();

                Log.i(TAG, "File has been closed?");
                Log.i(TAG, "byteArrayList size: "+bytearraylist.size());
            } catch (FileNotFoundException fnfe){
                Log.i(TAG, "File not made yet?");
            }

            byte[] finalOutputByte = finalOutput.getBytes();

            Log.i(TAG, "finalOutputByte length: "+finalOutputByte.length);

            for (int i = 0; i < finalOutputByte.length; ++i) {
                bytearraylist.add(finalOutputByte[i]);
            }

            Log.i(TAG, "byteArrayList size: "+bytearraylist.size());

            //Conversion to byte[]
            byte[] finalByteArray = new byte[bytearraylist.size()];
            for(int i = 0; i < bytearraylist.size(); ++i){
                finalByteArray[i] = bytearraylist.get(i);
            }

            Log.i(TAG, "finalByteArray length: "+finalByteArray.length);

            //File Overwriting
            FileOutputStream fos = openFileOutput(OUTPUTFILENAME, Context.MODE_PRIVATE);
            fos.write(finalByteArray);
            fos.close();
            Log.i(TAG, "File has been overwritten?");
        } catch(FileNotFoundException fnfe){
            Log.i(TAG, fnfe.toString());
        } catch(IOException ioe){
            Log.i(TAG, ioe.toString());
        }

        //insert code here to save data locally
        DialogFragment dfrag = new ActionCompleteDialogFragment();
        dfrag.show(getFragmentManager(), "Tag");
    }

    @Override
    public void onGoBack(DialogFragment dialog) {
        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(i);
        finish();
    }
}
