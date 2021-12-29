package com.t.ocslockapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

/**
 * Custom progress bar
 */
public class CustomProgressBar extends Dialog {

    Activity activity;
    Context context;

    public CustomProgressBar(Activity act) {
        super(act);
        this.activity = act;
    }

    public CustomProgressBar(Context act) {
        super(act);
        this.context = act;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.custom_progressbar);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


    }
}
