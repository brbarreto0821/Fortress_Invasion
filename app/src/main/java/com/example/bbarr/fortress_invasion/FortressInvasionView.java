package com.example.bbarr.fortress_invasion;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class FortressInvasionView extends SurfaceView implements Runnable{

    Context context;

    private Thread gameThread = null;

    // Is a SurfaceHolder to lock the surface before drawing the graphics
    private SurfaceHolder ourHolder;

    // A boolean which will be set and unset
    // when the game is running or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // Used to help calculate the fps
    private long timeThisFrame;

    // The size of the screen
    private int screenX;
    private int screenY;

    // The Players defending ship
    private Defender defender;

    // The player's arrows
    private Arrows arrow;

    // The com.example.bbarr.fortress_invasion.Invaders' arrows
    private Arrows[] invadersProjectile = new Arrows[200];
    private int nextArrow;
    private int maxInvaderProjectile = 10;

    // This represents how many invaders will be onscreen
    // Up to 60 invaders
    Invaders[] invaders = new Invaders[60];
    int numInvaders = 0;

    // This will be the bricks used to protect the player
    private BrickWalls[] bricks = new BrickWalls[400];
    private int numBricks;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // The score
    int score = 0;

    // Lives
    private int lives = 3;

    // The menacing level of the sound
    private long menaceInterval = 1000;
    // Chooses which menace sound should be next
    private boolean uhOrOh;
    // Checks when the last time the sound was played
    private long lastMenaceTime = System.currentTimeMillis();




    // When the we initialize (call new()) on gameView
    // This special constructor method runs
    public FortressInvasionView(Context context, int x, int y)
    {

        //Asks the SurfaceView class to set up object.
        super(context);

        // Make a globally available copy of the context so we can use it in another method
        this.context = context;

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;


        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try
        {

            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Loads the fx in memory so it can be used
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        }

        catch(IOException e)
        {
            // Prints an error message if there is an error
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel(){

        // Make a new player defender ship
        defender = new Defender(context, screenX, screenY);
        // This will create the player's arrows
        arrow = new Arrows(screenY);
        // Initialize the invadersProjectile array
        for(int i = 0; i < invadersProjectile.length; i++)
        {
            invadersProjectile[i] = new Arrows(screenY);
        }
        // Builds an army of invaders
        numInvaders = 0;
        for(int column = 0; column < 6; column ++ )
        {
            for(int row = 0; row < 5; row ++ )
            {
                invaders[numInvaders] = new Invaders(context, row, column, screenX, screenY);
                numInvaders ++;
            }
        }
        // Builds the brick walls
        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++)
        {
            for(int column = 0; column < 10; column ++ )
            {
                for (int row = 0; row < 5; row++)
                {
                    bricks[numBricks] = new BrickWalls(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }
    }


    @Override
    public void run()
    {
        while (playing)
        {

            // This captures the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Updates the frame
            if (!paused)
            {
                update();
            }

            // Draws the frame
            draw();

            // Calculates the fps of this frame.
            // This will then use the result to
            // time animations.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
            {
                fps = 1000 / timeThisFrame;
            }

            // Plays a sound based on the menace level
            if(!paused)
            {
                if ((startFrameTime - lastMenaceTime) > menaceInterval)
                {
                    if (uhOrOh)
                    {
                        // Play Uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    }
                    else
                    {
                        // Play Oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Resets the last menace time
                    menaceInterval = 1000;
                    lastMenaceTime = System.currentTimeMillis();
                    // Alters the value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }

        }
    }

    private void update()
    {

        // Checks if invaders bumped into the side of the screen
        boolean bumped = false;

        // This checks if the player has lost
        boolean lost = false;

        // Moves the defender
        defender.update(fps);
        // Updates all the invaders if visible
        for(int i = 0; i < numInvaders; i++)
        {

            if(invaders[i].getVisibility())
            {
                // Moves the next invader
                invaders[i].update(fps);

                // Checks if the defender will take a shot
                if(invaders[i].takeAim(defender.getX(),
                        defender.getLength())){

                    // If so then an arrow will spawn
                    if(invadersProjectile[nextArrow].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), arrow.DOWN))
                    {

                        // Shot fired
                        // Prepares for the next shot
                        nextArrow++;

                        // Loop back to the first one if it has reached the last
                        if (nextArrow == maxInvaderProjectile)
                        {
                            // This stops the firing of another arrow until one completes its journey
                            // Because if arrow 0 is still active, shoot returns false.
                            nextArrow = 0;
                        }
                    }
                }

                // If the move caused the invaders to bump the screen change bumped to true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0)
                {

                    bumped = true;

                }
            }

        }
        // This updates all the invaders projectiles if they are active
        for(int i = 0; i < invadersProjectile.length; i++)
        {
            if(invadersProjectile[i].getStatus())
            {
                invadersProjectile[i].update(fps);
            }
        }
        // Checks if the invaders bumped into the edge of the screen
        if(bumped)
        {

            // Move all the invaders down and change direction
            for(int i = 0; i < numInvaders; i++)
            {
                invaders[i].dropDownAndReverse();
                // Checks to see if the invaders have infiltrated
                // the defender's defences
                if(invaders[i].getY() > screenY - screenY / 10)
                {
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }
        // If player loses then the game will restart
        if (lost)
        {
            prepareLevel();
        }

        // Updates the players arrows
        if(arrow.getStatus())
        {
            arrow.update(fps);
        }
        // Checks if player's arrow hit the top of the screen
        if(arrow.getImpactPointY() < 0)
        {
            arrow.setInactive();
        }
        // Checks if invader's projectile hit the bottom of the screen
        for(int i = 0; i < invadersProjectile.length; i++)
        {
            if(invadersProjectile[i].getImpactPointY() > screenY)
            {
                invadersProjectile[i].setInactive();
            }
        }
        // Checks if player's arrow hit an invader
        if(arrow.getStatus())
        {
            for (int i = 0; i < numInvaders; i++)
            {
                if (invaders[i].getVisibility())
                {
                    if (RectF.intersects(arrow.getRect(), invaders[i].getRect()))
                    {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        arrow.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10)
                        {
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }
        // Checks if an invader's arrow hit a brick wall
        for(int i = 0; i < invadersProjectile.length; i++)
        {
            if(invadersProjectile[i].getStatus())
            {
                for(int j = 0; j < numBricks; j++)
                {
                    if(bricks[j].getVisibility())
                    {
                        if(RectF.intersects(invadersProjectile[i].getRect(), bricks[j].getRect()))
                        {
                            // A collision has occurred
                            invadersProjectile[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }
        // Has a player arrow hit a shelter brick
        if(arrow.getStatus()){
            for(int i = 0; i < numBricks; i++)
            {
                if(bricks[i].getVisibility())
                {
                    if(RectF.intersects(arrow.getRect(), bricks[i].getRect()))
                    {
                        // A collision has occurred
                        arrow.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }
        // Checks if an invader's arrow hit the player
        for(int i = 0; i < invadersProjectile.length; i++)
        {
            if(invadersProjectile[i].getStatus())
            {
                if(RectF.intersects(defender.getRect(), invadersProjectile[i].getRect()))
                {
                    invadersProjectile[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if(lives == 0)
                    {
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }
    }

    private void draw()
    {
        if (ourHolder.getSurface().isValid())
        {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.argb(255, 0, 128, 0));

            // The color for the bricks
            paint.setColor(Color.argb(255, 142, 37, 37));


            // Draw the defender
            canvas.drawBitmap(defender.getBitmap(), defender.getX(), screenY - 50, paint);
            // Draw the invaders
            for(int i = 0; i < numInvaders; i++)
            {
                if(invaders[i].getVisibility())
                {
                    if(uhOrOh)
                    {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                    else
                    {
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }
            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++)
            {
                if(bricks[i].getVisibility())
                {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }
            // Draw the players arrows
            paint.setColor(Color.argb(255, 255, 255, 255));
            if(arrow.getStatus())
            {
                canvas.drawRect(arrow.getRect(), paint);
            }
            // Draw the invaders projectiles if alive
            paint.setColor(Color.argb(255, 255, 0, 0));
            for(int i = 0; i < invadersProjectile.length; i++)
            {
                if(invadersProjectile[i].getStatus())
                {
                    canvas.drawRect(invadersProjectile[i].getRect(), paint);
                }
            }
            // Draws the score and remaining lives
            // Change the brush color
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // If MainActivity is paused/stopped
    // shutdown our thread.
    public void pause()
    {
        playing = false;
        try
        {
            gameThread.join();
        } catch (InterruptedException e)
        {
            Log.e("Error:", "joining thread");
        }

    }

    // If MainActivity is started then
    // start our thread.
    public void resume()
    {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
        {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                paused = false;

                if(motionEvent.getY() > screenY - screenY / 8)
                {
                    if (motionEvent.getX() > screenX / 2)
                    {
                        defender.setMovementState(defender.RIGHT);
                    }
                    else
                    {
                        defender.setMovementState(defender.LEFT);
                    }

                }

                if(motionEvent.getY() < screenY - screenY / 8)
                {
                    // Shots fired
                    if(arrow.shoot(defender.getX()+ defender.getLength()/2,screenY,arrow.UP))
                    {
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                if(motionEvent.getY() > screenY - screenY / 10)
                {
                    defender.setMovementState(defender.STOPPED);
                }

                break;

        }

        return true;
    }
}