package com.example.bbarr.fortress_invasion;


import android.graphics.RectF;

public class BrickWalls
{
    private RectF rect;

    private boolean isVisible;

    public BrickWalls(int row, int column, int wallNumber, int screenX, int screenY)
    {

        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;


        int brickPadding = 0;

        // The number of brick walls
        int wallPadding = screenX / 9;
        int startHeight = screenY - (screenY /8 * 2);

        rect = new RectF(column * width + brickPadding + (wallPadding * wallNumber) +
                wallPadding + wallPadding * wallNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (wallPadding * wallNumber) + wallPadding + wallPadding * wallNumber,
                row * height + height - brickPadding + startHeight);
    }

    public RectF getRect(){
        return this.rect;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }
}

