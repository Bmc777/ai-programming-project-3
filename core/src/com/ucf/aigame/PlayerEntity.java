package com.ucf.aigame;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bryan on 2/3/2016.
 */
public class PlayerEntity
{
    // Sensors and collisions
    private Rectangle collisionBox;
    private WallSensor wallSensor;
    private AdjacentAgentSensor radar;
    private PieSliceSensor pieSliceSensor;

    // Velocity and heading
    private Vector2 currentPlayerVelocity;
    private Vector2 nextPlayerVelocity;
    private Vector2 currentPlayerHeading;   //Direction player is facing (Should always be Normalized)
    private Vector2 nextPlayerHeading;

    boolean collisionUp;
    boolean collisionDown;
    boolean collisionLeft;
    boolean collisionRight;
    boolean currentlyHaveCollision;

    private int inputX;
    private int inputY;

    // Global and local positioning
    private float playerWidth;
    private float playerHeight;
    private float xPlayerOrigin;
    private float yPlayerOrigin;
    private float xCurrentWorldPosition;
    private float yCurrentWorldPosition;

    private float rotationAngle; //Angle between current and next Heading

    private Vector2 goalLocation;

    private static final float BASE_VELOCITY = 125;
    private static final Vector2 REFERENCE_VECTOR = new Vector2(1, 0);  //Normalized Vector pointing to 0 degrees

    private static final float MAX_SPEED = 250;

    private AStarSearch aStarSearch;

    PlayerEntity(float xCurrentWorldPosition, float yCurrentWorldPosition, float playerWidth, float playerHeight, GameWorld gameWorld)
    {
        //Player Sprite dimensions
        this.playerWidth = playerWidth;
        this.playerHeight = playerHeight;

        // GOAL for A* Search :: 1 < x < 38 , 1 < y < 18
        this.goalLocation = new Vector2( playerWidth*38 - 16, playerWidth*18 - 16);

        aStarSearch = new AStarSearch(this, gameWorld.goalLocation, gameWorld);

        //Center of the Sprite
        this.xPlayerOrigin = playerWidth / 2;
        this.yPlayerOrigin = playerHeight / 2;

        //Spawn Position
        this.xCurrentWorldPosition = xCurrentWorldPosition;
        this.yCurrentWorldPosition = yCurrentWorldPosition;

        currentPlayerHeading = new Vector2(REFERENCE_VECTOR);       //Player always spawns facing 'East'
        nextPlayerHeading = new Vector2(currentPlayerHeading);
        currentPlayerVelocity = new Vector2();                      //Velocity is initially 0
        nextPlayerVelocity = new Vector2(currentPlayerVelocity);

        wallSensor = new WallSensor(playerWidth * 6);
        collisionBox = new Rectangle(xCurrentWorldPosition, yCurrentWorldPosition, playerWidth, playerHeight);

        radar = new AdjacentAgentSensor(playerWidth * 6, xCurrentWorldPosition+xPlayerOrigin, yCurrentWorldPosition+yPlayerOrigin);
        pieSliceSensor = new PieSliceSensor(currentPlayerHeading, playerWidth * 6);


    }

    public void update(float timeSinceLastUpdate)
    {
        aStarSearch.update();

        currentPlayerHeading.set(nextPlayerHeading);    //Update to new calculated heading
        rotationAngle = currentPlayerHeading.angle();   //Angle of the new heading with respect to X axis.

        nextPlayerVelocity.set(inputX, inputY);         //Velocity initialized to basic input velocities

        if (inputX != 0 && inputY != 0)
        {
            nextPlayerVelocity.scl(0.5f);               //Diagonal movement should not be faster
        }

        nextPlayerVelocity.scl(BASE_VELOCITY);          //Applying the velocity magnitude
        nextPlayerVelocity.rotate(rotationAngle - 90);  //Rotating the vector to match the Sprite
        nextPlayerVelocity.limit(MAX_SPEED);            //
        currentPlayerVelocity.set(nextPlayerVelocity);  //Update the current velocity

        if (currentlyHaveCollision)
        {
            if (collisionUp && currentPlayerVelocity.y > 0)
                currentPlayerVelocity.y = 0;

            if (collisionDown && currentPlayerHeading.y < 0)
                currentPlayerVelocity.y = 0;

            if (collisionLeft && currentPlayerHeading.x < 0)
                currentPlayerVelocity.x = 0;

            if (collisionRight && currentPlayerHeading.x > 0)
                currentPlayerVelocity.x = 0;
        }

        //Update World position, scaling velocity over timeSinceLastUpdate
        xCurrentWorldPosition += currentPlayerVelocity.x * timeSinceLastUpdate;
        yCurrentWorldPosition += currentPlayerVelocity.y * timeSinceLastUpdate;

        collisionBox.setPosition(xCurrentWorldPosition, yCurrentWorldPosition);
        wallSensor.update(currentPlayerHeading);

        radar.update(xCurrentWorldPosition + xPlayerOrigin, yCurrentWorldPosition + yPlayerOrigin);
        pieSliceSensor.update(currentPlayerHeading);

        currentlyHaveCollision = false;


    }

    //Updated by InputHandler
    public void moveLeft()
    {
        inputX -= 1;
    }

    public void moveRight()
    {
        inputX += 1;
    }

    public void moveUp()
    {
        inputY += 1;
    }

    public void moveDown()
    {
        inputY -= 1;
    }

    public void stop() {
        inputX = inputY = 0;
    }

    public void rotateToFaceMouse(float xCurrentMousePosition, float yCurrentMousePosition)
    {
        //Determine the new heading vector offset by playerOrigin to align heading with center of sprite
        nextPlayerHeading.x = xCurrentMousePosition - (xCurrentWorldPosition + xPlayerOrigin);
        nextPlayerHeading.y = yCurrentMousePosition - (yCurrentWorldPosition + yPlayerOrigin);

        //Normalize the heading vector
        nextPlayerHeading.nor();
    }

    public void setNextPlayerHeading(float xNewHeading, float yNewHeading) {
        nextPlayerHeading.x = xNewHeading;
        nextPlayerHeading.y = yNewHeading;
        nextPlayerHeading.nor();
    }

    public void setNextPlayerHeading(Vector2 newHeading) {
        nextPlayerHeading = newHeading;
        nextPlayerHeading.nor();
    }

   public float getCurrentXPosition()
    {
        return xCurrentWorldPosition;
    }

    public float getCurrentYPosition()
    {
        return yCurrentWorldPosition;
    }

    public float getRotationAngle()
    {
        return rotationAngle;
    }

    public float getWidth()
    {
        return playerWidth;
    }

    public float getHeight()
    {
        return playerHeight;
    }

    public float getXPlayerOrigin()
    {
        return xPlayerOrigin;
    }

    public float getYPlayerOrigin()
    {
        return yPlayerOrigin;
    }

    public float getWallSensorEndpointX(int i)
    {
        return wallSensor.getSensor(i).x + xCurrentWorldPosition + xPlayerOrigin;
    }

    public float getWallSensorEndpointY(int i)
    {
        return wallSensor.getSensor(i).y + yCurrentWorldPosition + yPlayerOrigin;
    }

    public float getWallSensorOriginX()
    {
        return xCurrentWorldPosition + xPlayerOrigin;
    }

    public Vector2[] getWallSensorArray()
    {
        return wallSensor.getWallSensorArray();
    }

    public float getWallSensorOriginY()
    {
        return yCurrentWorldPosition + yPlayerOrigin;
    }

    public void setWallSensorLength(float length, int index)
    {
        wallSensor.setLength(length, index);
    }

    public float[] getWallSensorLengthArray()
    {
        return wallSensor.getLengthArray();
    }

    public void setWallSensorCollisionIndex(int index, boolean value)
    {
        wallSensor.setCollisionArrayIndex(index, value);
    }

    public boolean getWallSensorCollisionValue(int index)
    {
        return wallSensor.getCollisionValue(index);
    }

    public Rectangle getCollisionBox()
    {
        return collisionBox;
    }

    public void setCollisionDetection(boolean collisionUp, boolean collisionDown, boolean collisionLeft, boolean collisionRight)
    {
        this.collisionUp = collisionUp;
        this.collisionDown = collisionDown;
        this.collisionLeft = collisionLeft;
        this.collisionRight = collisionRight;
    }

    public void setCollisionNotification(boolean currentlyHaveCollision)
    {
        this.currentlyHaveCollision = currentlyHaveCollision;
    }

    public Vector2 getvOrigin() {
        return radar.getvOrigin();
    }

    public Vector2 getCurrentHeading() {
        return currentPlayerHeading;
    }

    public Circle getAdjecentAgentSensor() {
        return radar.getSensor();
    }

    public PieSliceSensor getPieSliceSensor() {
        return pieSliceSensor;
    }

    public void setController(InputHandler ctrl) {
        aStarSearch.setInputHandler(ctrl);
    }

    public AStarSearch getAStarSearch() {
        return aStarSearch;
    }
}
