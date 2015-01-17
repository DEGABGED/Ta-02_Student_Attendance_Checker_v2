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
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class SendDataActivity extends ActionBarActivity {

    static final int REQUEST_CODE_PICK_ACCOUNT = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 2;

    private static String TAG = "Student.Attendance";

    static String email;
    static String scope = "oauth2:https://spreadsheets.google.com/feeds";
    static SpreadsheetService sheetService;
    static CharSequence[] sheets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
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
                new GetUsernameTask(SendDataActivity.this, email, scope).execute();
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

                    try {
                        SheetEdit se = new SheetEdit(sheetService, new PrintStream(System.out));
                        List sheetlist = se.getSheetList();
                        sheets = new CharSequence[sheetlist.size()];
                        for (int j = 0; j < sheetlist.size(); j++) {
                            sheets[j] = se.showSheet(j, sheetlist);
                            Log.i("Sheetlist", se.showSheet(j, sheetlist));
                        }

                        DialogFragment dfrag = new SheetChoiceDialogFragment();
                        SheetChoiceDialogFragment.setSheetlist(sheets);
                        dfrag.show(getFragmentManager(), "Tag");

                    } catch (AuthenticationException authEx){
                        Log.e(TAG, "An Auth Exception occurred", authEx);
                        Log.i(TAG, Log.getStackTraceString(authEx));
                    } catch (IOException io){
                        Log.i(TAG, "An IOException occurred");
                        Log.i(TAG, io.toString());
                    } catch (ServiceException se){
                        Log.i(TAG, "A Service Exception occurred");
                        Log.i(TAG, se.toString());
                    } catch (Exception e){
                        Log.i(TAG, "A different exception occurred");
                        Log.i(TAG, e.toString());
                    }
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

    public void showSheetChoiceDialog(){

    }

    public void onAccChooseClick(View view){
        if(isInternetPresent()){
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            Log.i(TAG, "Account Picker Intent will be sent");
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);

        } else {
            Log.i(TAG, "Not connected to Internet");
        }
    }
}