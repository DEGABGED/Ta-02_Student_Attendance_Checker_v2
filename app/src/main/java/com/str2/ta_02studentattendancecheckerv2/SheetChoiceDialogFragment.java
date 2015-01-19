package com.str2.ta_02studentattendancecheckerv2;

import android.app.Activity;
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
    public interface SheetChoiceDialogListener {
        public void onSheetClick(DialogFragment dialog, boolean isSpreadsheet, int index);
    }

    SheetChoiceDialogListener scdListener;

    private static CharSequence[] spreadsheets;
    public static boolean isSpreadsheet; //true = spreadsheet; false = worksheet

    public static void setSheetlist(CharSequence[] sheetlist){
        spreadsheets = sheetlist;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            scdListener = (SheetChoiceDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SCDListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose the destination " + (isSpreadsheet ? "spread" : "work") + "sheet")
                .setItems(spreadsheets, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("SheetChoice", "Chosen sheet is: " + spreadsheets[which]);
                        scdListener.onSheetClick(SheetChoiceDialogFragment.this, isSpreadsheet, which);
                    }
                });
        return builder.create();
    }
}
