package com.example.bbarr.fortress_invasion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Defender
{
    RectF rect;

    // The Defender will be represented by a Bitmap
    private Bitmap bitmap;

    // How long and high the Defender will be
    private float length;
    private float height;

    // X is the far left of the rectangle which forms the Defender
    private float x;

    // Y is the top coordinate
    private float y;

    // This will hold the pixels per second speed that the Defender will move
    private float defenderSpeed;

    // Which ways can the Defender move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Checks to see if Defender is moving or not
    private int defenderMoving = STOPPED;



    // This is the constructor method
    public Defender(Context context, int screenX, int screenY)
    {

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX/15;
        height = screenY/19;

        // Start ship will be in center of screen
        x = screenX / 2;
        y = screenY - 20;

        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defender);

        // Stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // How fast is the Defender will be moving in pixels per second
        defenderSpeed = 350;
    }
    public RectF getRect(){
        return rect;
    }


    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getLength(){
        return length;
    }

    // This method will be used to change/set if the Defender is going left, right or nowhere
    public void setMovementState(int state){
        defenderMoving = state;
    }

    /* This update method will be called from update in FortressInvasionView
       It determines if the Defender needs to move and changes the coordinates
       contained in x if necessary */
    public void update(long fps)
    {
        if(defenderMoving == LEFT){
            x = x - defenderSpeed / fps;
        }

        if(defenderMoving == RIGHT){
            x = x + defenderSpeed / fps;
        }

        // This updates rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }

}