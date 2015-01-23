package com.str2.ta_02studentattendancecheckerv2;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.util.InvalidEntryException;
import com.google.gdata.util.ServiceException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class SendDataActivity extends ActionBarActivity implements SheetChoiceDialogFragment.SheetChoiceDialogListener,
        ActionCompleteDialogFragment.ActionCompleteDialogListener {

    static final int REQUEST_CODE_PICK_ACCOUNT = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 2;

    private static String TAG = "Student.Attendance";

    static String email;
    static String scope = "oauth2:https://spreadsheets.google.com/feeds";
    static SpreadsheetService sheetService;
    static CharSequence[] sheets, wsheets;
    static SheetEdit se;
    static SpreadsheetEntry spreadsheet;

    static TextView nEmail;
    static TextView nSpreadsheet;
    static TextView nWorksheet;

    static boolean isMultipleWorksheets = false;

    GetUsernameTask gut;
    ChooseWorksheetTask cwt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        nEmail = (TextView) findViewById(R.id.nEmail);
        nSpreadsheet = (TextView) findViewById(R.id.nSpreadsheet);
        nWorksheet = (TextView) findViewById(R.id.nWorksheet);
    }

    @Override
    protected void onDestroy(){
        Log.i(TAG, "destroysed");
        if(gut != null){
            gut.cancel(true);
        }
        if(cwt != null){
            cwt.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        Log.i(TAG, "paused");
        Log.i(TAG, "canceled");
        super.onPause();
    }

    @Override
    protected void onStop(){
        Log.i(TAG, "stoped");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_data, menu);
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

    public boolean isInternetPresent(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                email = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                Log.i(TAG, "Account is chosen");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nEmail.setText("email: " + email);
                    }
                });
                Toast.makeText(this.getApplicationContext(),
                        "Account has been chosen. Please wait for a dialog to pop up for choosing the destination spreadsheet.",
                        Toast.LENGTH_SHORT).show();
                gut = new GetUsernameTask(SendDataActivity.this, email, scope);
                gut.execute();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Log.i(TAG, "Account not picked yet");
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR){
            if(resultCode == RESULT_OK){
                Log.i(TAG, "Access granted");
                new GetUsernameTask(SendDataActivity.this, email, scope).execute();
            } else {
                Log.i(TAG, "Access denied");
                onAccChooseClick(null);
            }
        }
    }

    public class GetUsernameTask extends AsyncTask<String, Void, Void> {
        Activity activity;
        String scope;
        String email;

        GetUsernameTask(Activity activity, String name, String scope) {
            this.scope = scope;
            this.email = name;
            this.activity = activity;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(activity, email, scope);
            } catch (UserRecoverableAuthException userAuthEx) {
                Log.i(TAG, "A UserRecoverableAuthException occurred");
                //app not authorized for account
                Intent intent = userAuthEx.getIntent();
                activity.startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            } catch (GoogleAuthException fatalEx) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                Log.i(TAG, "A GoogleAuthException occurred");
                Log.i(TAG, fatalEx.getMessage());
            }
            return null;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String token = fetchToken();
                if (token != null) {
                    // Insert the good stuff here.
                    // Use the token to access the user's Google data.
                    sheetService = new SpreadsheetService("Ta-02 Student Attendance Checker");
                    sheetService.setOAuth2Credentials(new Credential(BearerToken
                            .authorizationHeaderAccessMethod())
                            .setFromTokenResponse(new TokenResponse().setAccessToken(token)));
                    Log.i(TAG, token);
                    se = new SheetEdit(sheetService);
                    chooseSpreadsheet();
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                Log.i(TAG, "An IOException occurred");
            }
            return null;
        }
    }

    @Override
    public void onSheetClick(DialogFragment dialog, boolean isSpreadsheet, int index) {
        if(isSpreadsheet) {
                    nSpreadsheet.setText("spreadsheet: " + "" + sheets[index]);
            Toast.makeText(this.getApplicationContext(),
                    "Spreadsheet has been chosen. Please wait in case a worksheet requires to be chosen.",
                    Toast.LENGTH_SHORT).show();
        }
        cwt = new ChooseWorksheetTask(isSpreadsheet, index, this);
        cwt.execute();
    }

    public class ChooseWorksheetTask extends AsyncTask<Void, Void, Void>{
        boolean isSpreadsheet;
        int index;
        Activity activity;

        ChooseWorksheetTask(boolean isSpreadsheet, int index, Activity activity){
            this.isSpreadsheet = isSpreadsheet;
            this.index = index;
            this.activity = activity;
        }


        @Override
        protected Void doInBackground(Void... params) {
            if(isSpreadsheet){
                //it's a spreadsheet; check for worksheets
                try {
                    spreadsheet = se.loadSpreadsheet(index);
                    Log.i(TAG, "Spreadsheet chosen: " + sheets[index]);
                    if(spreadsheet.getWorksheets().size() == 1){
                        isMultipleWorksheets = false;
                        se.loadWorksheet(spreadsheet, 0);
                        Log.i(TAG, "just 1 worksheet");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Worksheet loaded.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        isMultipleWorksheets = true;
                        List worksheets = spreadsheet.getWorksheets();
                        wsheets = new CharSequence[worksheets.size()];
                        for (int j = 0; j < worksheets.size(); j++) {
                            wsheets[j] = se.showSheet(j, worksheets);
                            Log.i("Sheetlist", se.showSheet(j, worksheets));
                        }

                        DialogFragment dfrag = new SheetChoiceDialogFragment();
                        SheetChoiceDialogFragment.setSheetlist(wsheets);
                        SheetChoiceDialogFragment.isSpreadsheet = false;
                        dfrag.show(getFragmentManager(), "Tag");
                    }
                } catch (ServiceException serex){
                    Log.e(TAG, "A service exception occurred");
                    Log.i(TAG, serex.toString());
                } catch (IOException io){
                    Log.e(TAG, "An IO exception occurred");
                    Log.i(TAG, io.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sorry, an error occurred. Try checking your internet connectivity.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                //it's a worksheet; start writing
                try {
                    se.loadWorksheet(spreadsheet, index);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nWorksheet.setText("worksheet: " + wsheets[index]);
                            Toast.makeText(getApplicationContext(), "Worksheet loaded.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ServiceException serex){
                    Log.e(TAG, "A service exception occurred");
                    Log.i(TAG, serex.toString());
                } catch (IOException io){
                    Log.e(TAG, "An IO exception occurred");
                    Log.i(TAG, io.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sorry, an error occurred. Try checking your internet connectivity.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            return null;
        }
    }

    public void chooseSpreadsheet(){
        try {
            List sheetlist = se.getSheetList();
            sheets = new CharSequence[sheetlist.size()];
            for (int j = 0; j < sheetlist.size(); j++) {
                sheets[j] = se.showSheet(j, sheetlist);
                Log.i("Sheetlist", se.showSheet(j, sheetlist));
            }
                DialogFragment dfrag = new SheetChoiceDialogFragment();
                SheetChoiceDialogFragment.setSheetlist(sheets);
                SheetChoiceDialogFragment.isSpreadsheet = true;
                dfrag.show(getFragmentManager(), "Tag");
        } catch (ServiceException serex){
            Log.e(TAG, "A service exception occurred");
            Log.i(TAG, serex.toString());
        } catch (IOException io){
            Log.e(TAG, "An IO exception occurred");
            Log.i(TAG, io.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Sorry, an error occurred. Try checking your internet connectivity.", Toast.LENGTH_SHORT).show();
               }
            });
        }
    }

    public void writeOnSheet(View view){
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                String actualText = "";
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

                ArrayList<String> lines = new ArrayList<>();
                int previousIndex = 0;
                for(int i = actualText.indexOf("]"); i < actualText.length(); i++){
                    if((actualText.charAt(i)+"").equals("]")){
                        lines.add(actualText.substring(previousIndex, i+1));
                        previousIndex = i+1;
                    }
                }

                Log.i(TAG, "no. of lines is "+lines.size());

                    for(int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        String subject = line.substring(0, line.indexOf("_"));
                        String period = line.substring(line.indexOf("_")+1, line.indexOf("_", line.indexOf("_")+1));
                        String classlist = line.substring(line.indexOf("["), line.indexOf("]"));

                        Date date = new Date();
                        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                        String time = df.format(date);
                        Log.i("Time", time);

                        ListEntry row = new ListEntry();
                        row.getCustomElements().setValueLocal("Timestamp", time);
                        row.getCustomElements().setValueLocal("Subject", subject);
                        row.getCustomElements().setValueLocal("Period", period);

                        int cn = 0;
                        for(int j = 0; j < classlist.length(); j++){
                            if((classlist.charAt(j)+"").equals("0") || (classlist.charAt(j)+"").equals("1") || (classlist.charAt(j)+"").equals("2")){
                                cn++;
                                int pta = Integer.parseInt(classlist.charAt(j)+"");
                                switch(pta){
                                    case 0:
                                        row.getCustomElements().setValueLocal("CN" + ((cn < 10) ? ("0"+cn) : (cn)), "PRESENT");
                                        Log.i(TAG, "CN" + ((cn < 10) ? ("0"+cn) : (cn)) + "PRESENT");
                                        break;
                                    case 1:
                                        row.getCustomElements().setValueLocal("CN" + ((cn < 10) ? ("0"+cn) : (cn)), "TARDY");
                                        Log.i(TAG, "CN" + ((cn < 10) ? ("0"+cn) : (cn)) + "TARDY");
                                        break;
                                    case 2:
                                        row.getCustomElements().setValueLocal("CN" + ((cn < 10) ? ("0"+cn) : (cn)), "ABSENT");
                                        Log.i(TAG, "CN" + ((cn < 10) ? ("0"+cn) : (cn)) + "ABSENT");
                                        break;
                                }
                            }
                        }

                        se.setRow(row);
                    }

                    deleteFile(LogActivity.OUTPUTFILENAME);

                    DialogFragment dfrag = new ActionCompleteDialogFragment();
                    dfrag.show(getFragmentManager(), "Tag");
                } catch (FileNotFoundException fnfe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "File not found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (InvalidEntryException iee) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Wrong spreadsheet/worksheet or sheet format. Go back and try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ServiceException serex){
                    Log.e(TAG, "A service exception occurred");
                    Log.i(TAG, serex.toString());
                } catch (IOException io){
                    Log.e(TAG, "An IO exception occurred");
                    Log.i(TAG, io.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sorry, an error occurred. Try checking your internet connectivity.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        if(se != null){
            if((se.getListFeedUrl() != null) || (!isMultipleWorksheets)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Sending data...", Toast.LENGTH_SHORT).show();
                    }
                });
                thr.start();
            } else {
                Log.i(TAG, "worksheet not loaded yet");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Please choose worksheet first.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Log.i(TAG, "se not loaded yet");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Please choose spreadsheet first.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void onAccChooseClick(View view){
        if(isInternetPresent()){
            if(gut != null){
                gut.cancel(true);
            }
            if(cwt != null){
                cwt.cancel(true);
            }
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            Log.i(TAG, "Account Picker Intent will be sent");
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        } else {
            Log.i(TAG, "Not connected to Internet");
            Toast.makeText(this.getApplicationContext(),
                    "Not connected to Internet.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGoBack(DialogFragment dialog) {
        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(i);
        finish();
    }
}
