package com.lib.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lib.http.IHttpRequester;
import com.lib.tools.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
