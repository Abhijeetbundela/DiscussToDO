package com.example.letsdiscusstodo.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.example.letsdiscusstodo.R;

public class MyProgressDialog extends AlertDialog {


    private CharSequence message = "Loading";
    private CharSequence title = "Title";

    //layout elements
    private TextView titleText;
    private TextView messageText;

    public MyProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.progress_dialog);
        setCanceledOnTouchOutside(true);

        titleText = findViewById(R.id.progressTitle);
        messageText = findViewById(R.id.progressMessage);

        titleText.setText(title);
        messageText.setText(message);


    }


    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        if (isShowing()) {

            initTitle();

        }

    }

    private void initTitle() {
        if (title != null && title.length() > 0) {

            titleText.setText(title);
        }

    }

    @Override
    public void setMessage(CharSequence message) {
        this.message = message;

        if (isShowing()) {

            initMessage();

        }
    }


    private void initMessage() {
        if (message != null && message.length() > 0) {

            messageText.setText(message);
        }
    }


}

