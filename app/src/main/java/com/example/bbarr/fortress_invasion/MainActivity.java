package com.example.bbarr.fortress_invasion;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends Activity
{

    /*fortressInvasionView is the view of the game
     This holds the logic of the game
     and responds to screen touches */
    FortressInvasionView fortressInvasionView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Loads the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Initializes the gameView and sets it as the view
        fortressInvasionView = new FortressInvasionView(this, size.x, size.y);
        setContentView(fortressInvasionView);

    }

    // This method will execute when the player starts the game
    @Override
    protected void onResume()
    {
        super.onResume();

        // This will tell the gameView resume method to execute
        fortressInvasionView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause()
    {
        super.onPause();

        // Tells the gameView pause method to execute
        fortressInvasionView.pause();
    }
}