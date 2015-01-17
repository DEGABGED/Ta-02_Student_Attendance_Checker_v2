package com.str2.ta_02studentattendancecheckerv2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by The Administrator on 1/17/2015.
 */
public class SheetChoiceDialogFragment extends DialogFragment{
    private static CharSequence[] spreadsheets;

    public static void setSheetlist(CharSequence[] sheetlist){
        spreadsheets = sheetlist;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose the destination spreadsheet")
                .setItems(spreadsheets, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("SheetChoice", "Chosen sheet is: "+spreadsheets[which]);
                        SendDataActivity.onSheetChoose(which);
                    }
                });
        return builder.create();
    }
}
