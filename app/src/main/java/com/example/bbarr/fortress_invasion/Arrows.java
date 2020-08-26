package com.example.bbarr.fortress_invasion;

import android.graphics.RectF;

public class Arrows
{
    private float x;
    private float y;

    private RectF rect;

    // Which way is the arrow shooting
    public final int UP = 0;
    public final int DOWN = 1;

    int heading = -1;
    float speed =  350;

    private int width = 1;
    private int height;

    private boolean isActive;

    public Arrows(int screenY)
    {

        height = screenY / 20;
        isActive = false;

        rect = new RectF();
    }
    public RectF getRect(){
        return  rect;
    }

    public boolean getStatus(){
        return isActive;
    }

    public void setInactive(){
        isActive = false;
    }

    public float getImpactPointY()
    {
        if (heading == DOWN)
        {
            return y + height;
        }
        else
        {
            return  y;
        }

    }
    public boolean shoot(float startX, float startY, int direction)
    {
        if (!isActive)
        {
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }

        // If arrow is active
        return false;
    }
    public void update(long fps)
    {

        // Moves up or down
        if(heading == UP)
        {
            y = y - speed / fps;
        }
        else
        {
            y = y + speed / fps;
        }

        // Updates rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
}
