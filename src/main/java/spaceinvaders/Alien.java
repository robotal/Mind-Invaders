package spaceinvaders;

import javax.swing.ImageIcon;
import java.awt.*;


public class Alien extends Sprite {


    private Bomb bomb;
    private final String shot = "../spacepix/bomb.png";


    public Alien(int x, int y){
        this.x=x;
        this.y=y;

        bomb = new Bomb(x,y);
        ImageIcon ii = new ImageIcon(this.getClass().getResource(shot));
        setImage(ii.getImage().getScaledInstance(Constants.SHOT_WIDTH, Constants.SHOT_HEIGHT, Image.SCALE_SMOOTH));
    }

    public void act (int direction){

        this.x+=direction;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public class Bomb extends Sprite {

        private String bomb = "../spacepix/bomb.png";
        private boolean destroyed;

        public Bomb(int x, int y){

            setDestroyed(true);
            this.x=x;
            this.y=y;
            ImageIcon ii = new ImageIcon(this.getClass().getResource(bomb));
            setImage(ii.getImage().getScaledInstance(Constants.SHOT_WIDTH, Constants.SHOT_HEIGHT, Image.SCALE_SMOOTH));
        }

        public void setDestroyed(boolean destroyed){
            this.destroyed = destroyed;
        }

        public boolean isDestroyed(){

            return destroyed;
        }
    }
}
