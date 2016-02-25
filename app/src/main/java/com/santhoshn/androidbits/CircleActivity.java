package com.santhoshn.androidbits;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.santhoshn.androidbits.views.Circle;

/**
 * Created by santhosh on 09/02/16.
 */
public class CircleActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        LinearLayout circlesLayout = (LinearLayout) this.findViewById(R.id.circles);

        circlesLayout.addView(new Circle(this, 10, 10, 100, 0xbbb));
        circlesLayout.addView(new Circle(this, 20, 20, 50, 0xaaa));
        circlesLayout.addView(new Circle(this, 30, 30, 25, 0xcccccc));

    }
}
