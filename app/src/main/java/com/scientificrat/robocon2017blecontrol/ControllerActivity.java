package com.scientificrat.robocon2017blecontrol;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.scientificrat.robocon2017blecontrol.util.AppVibrator;
import com.scientificrat.robocon2017blecontrol.util.MesurementUtility;
import com.scientificrat.robocon2017blecontrol.widget.CustomizableCommandButton;

public class ControllerActivity extends AppCompatActivity {

    private LinearLayout customizeButtonContainer = null;
    private DrawerLayout drawerLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        customizeButtonContainer = (LinearLayout) findViewById(R.id.customize_command_button_container);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerView.setClickable(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("SCU MVP");
//        setSupportActionBar(toolbar);

    }

    public void addCommandButton(View view) {
        //vibrate
        AppVibrator.vibrateShort(this);
        CustomizableCommandButton button = new CustomizableCommandButton(this);
        button.setBackground(getDrawable(R.drawable.blue_command_button));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        params.setMarginEnd(MesurementUtility.convertDPtoPX(getResources(), 8));
        customizeButtonContainer.addView(button, params);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_controller,menu);
//        return true;
//    }
}
