package spaceinvaders;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.ImageIcon;


public class Player extends Sprite {

    private final int START_Y = Constants.GROUND - Constants.PLAYER_HEIGHT;
    private final int START_X = Constants.BOARD_WIDTH/2;

    /*
    0 - right
    1 - left
    2 - up
    3 - down
     */
    private boolean directions_pressed[] = new boolean[4];
    private final int RIGHT_INDEX = 0, LEFT_INDEX = 1, UP_INDEX = 2, DOWN_INDEX = 3;

    private final String player = "../spacepix/player.png";

    Queue<Shot> shots = new ConcurrentLinkedQueue<Shot>();

    public Player() {

        ImageIcon ii = new ImageIcon(this.getClass().getResource(player));

        setImage(ii.getImage().getScaledInstance(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT, Image.SCALE_SMOOTH));
        setX(START_X);
        setY(START_Y);
    }

    public void act(double[] direction) {

        //System.out.println("Dir: " + direction[0]+", "+direction[1]);

        int [] velocity = getShift(direction);
        x += velocity[0];
        y += velocity[1];

        if (x <= 2)
            x = 2;
        if (x >= Constants.BOARD_WIDTH - Constants.PLAYER_WIDTH)
            x = Constants.BOARD_WIDTH - Constants.PLAYER_WIDTH;
        if(y <= 2){
            y = 2;
        }
        if( y >= Constants.GROUND - Constants.PLAYER_HEIGHT){
            y = Constants.GROUND - Constants.PLAYER_HEIGHT;
        }
    }

    private int [] getShift(double direction[]){

        int toRet[] =new int[2];
//        if(directions_pressed[LEFT_INDEX]){
//            direction[0] -= 1;
//        }
//        if(directions_pressed[RIGHT_INDEX]){
//            direction[0] += 1;
//        }
//        if(directions_pressed[UP_INDEX]){
//            direction[1] -= 1;
//        }
//        if(directions_pressed[DOWN_INDEX]){
//            direction[1] += 1;
//        }

        double mag = Math.pow(direction[0],2) + Math.pow(direction[1],2);

        if(mag == 0){

            toRet[0] = 0;
            toRet[1] = 0;
        }

        double deltaX = Constants.PLAYER_SPEED * direction[0];//Constants.PLAYER_SPEED* Math.sqrt(Math.abs(direction[0]/mag)) * direction[0];
        double deltaY = Constants.PLAYER_SPEED * direction[1];//Math.sqrt(Math.abs(direction[1]/mag)) * direction[1];

        toRet[0] = (int)deltaX;
        toRet[1] = (int)deltaY;

        return toRet;
    }

    public void keyPressed(KeyEvent e){

        int key = e.getKeyCode();

        if(key == KeyEvent.VK_LEFT){
            directions_pressed[LEFT_INDEX] = true;
        }

        if(key == KeyEvent.VK_RIGHT) {
            directions_pressed[RIGHT_INDEX] = true;
        }

        if(key == KeyEvent.VK_UP){

            directions_pressed[UP_INDEX] = true;
        }

        if(key == KeyEvent.VK_DOWN){

            directions_pressed[DOWN_INDEX] = true;
        }
    }

    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_LEFT){
            directions_pressed[LEFT_INDEX] = false;
        }
        if(key == KeyEvent.VK_RIGHT){
            directions_pressed[RIGHT_INDEX] = false;
        }
        if(key == KeyEvent.VK_UP){
            directions_pressed[UP_INDEX] = false;
        }
        if(key == KeyEvent.VK_DOWN){
            directions_pressed[DOWN_INDEX] = false;
        }
    }

    public void shoot(){
        Shot newShot = new Shot(getX(), getY());
        shots.add(newShot);
    }
}
