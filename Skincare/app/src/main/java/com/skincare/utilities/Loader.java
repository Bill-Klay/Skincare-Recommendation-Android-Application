package com.skincare.utilities;

import android.app.ProgressDialog;
import android.content.Context;

import com.skincare.R;

public class Loader {
    public static ProgressDialog show(Context context) {
        ProgressDialog dialog = new ProgressDialog(context, R.style.Loader);

        dialog.setMessage("Please wait...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        dialog.show();

        return dialog;
    }
}
