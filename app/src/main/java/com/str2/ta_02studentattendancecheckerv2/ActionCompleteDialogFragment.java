package com.str2.ta_02studentattendancecheckerv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by The Administrator on 1/21/2015.
 */
public class ActionCompleteDialogFragment extends DialogFragment{
    public interface ActionCompleteDialogListener {
        public void onGoBack(DialogFragment dialog);
    }

    ActionCompleteDialogListener acListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            acListener = (ActionCompleteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ACListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Action Complete.")
                .setNeutralButton("Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        acListener.onGoBack(ActionCompleteDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
