package spaceinvaders;

import connector.MuseOscServer;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.*;

public class Board extends JPanel implements Runnable {

    private Dimension d;
    private ArrayList<Alien> aliens;
    private Player player;

    private final int alienX = 150;
    private final int alienY = 5;
    private int direction = -1;
    private int deaths;

    private boolean ingame = true;
    private final String expl = "../spacepix/explosion.png";
    private final String alienpix = "../spacepix/alien.png";
    private String message = "Game Over";

    private Thread animator;

    private long lastShot = 0;
    private boolean spaceHeld = false;

    private boolean gameOver = false;

    private MuseOscServer server;

    public Board(MuseOscServer server){

        this.server = server;
        TAdapter adapter = new TAdapter();
        addKeyListener(adapter);
        addMouseListener(adapter);
        setFocusable(true);
        d = new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);

        setBackground(Color.black);

        gameInit();
        setDoubleBuffered(true);
    }

    public void gameInit(){

        message = "Game Over";
        deaths= 0;
        aliens = new ArrayList();

        ImageIcon ii = new ImageIcon(this.getClass().getResource(alienpix));

        for(int i=0; i < 4; i++){
            for(int j =0; j < 6; j++){
                Alien alien = new Alien(alienX + Constants.ALIEN_WIDTH*j, alienY + Constants.ALIEN_HEIGHT*i);
                alien.setImage(ii.getImage().getScaledInstance(Constants.ALIEN_WIDTH, Constants.ALIEN_HEIGHT, Image.SCALE_SMOOTH));
                aliens.add(alien);
            }
        }

        player = new Player();

        if(animator == null || !ingame) {

            ingame = true;
            animator = new Thread(this);
            animator.start();
            System.out.println("Game started");
        }
    }

    public void drawAliens(Graphics g){

        for (Alien alien : aliens) {
            if (alien.isVisible()) {
                g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
            }

            if (alien.isDying()) {
                alien.die();
            }
        }

    }

    public void drawPlayer(Graphics g){

        if(player.isVisible()){
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        }

        if(player.isDying()){
            player.die();
            ingame = false;
        }
    }

    public void drawShots(Graphics g) {
        for(Shot shot:player.shots) {
            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    public void drawBombing (Graphics g) {

        for (Object alien : aliens) {

            Alien a = (Alien) alien;

            Alien.Bomb b = a.getBomb();

            if (!b.isDestroyed()) {
                g.drawImage(b.getImage(), b.getX(), b.getY(), this);
            }
        }
    }

    public void paint(Graphics g){

        super.paint(g);

        g.setColor(Color.black);
        g.fillRect(0,0, d.width, d.height);
        g.setColor(Color.green);

        if(ingame){

            g.drawLine(0, Constants.GROUND, Constants.BOARD_WIDTH, Constants.GROUND);
            drawAliens(g);
            drawPlayer(g);
            drawShots(g);
            drawBombing(g);
        }

        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }

    public void gameOver(){

        gameOver = true;
        ingame = false;
        Graphics g = this.getGraphics();

        g.setColor(Color.black);
        g.fillRect(0,0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);

        g.setColor(new Color(0, 32,48));
        g.fillRect(50, Constants.BOARD_WIDTH/2 - 30, Constants.BOARD_WIDTH-100,50);
        g.setColor(Color.white);
        g.drawRect(50, Constants.BOARD_WIDTH/2 -30, Constants.BOARD_WIDTH-100, 50);

        g.drawRect(Constants.BOARD_WIDTH/2 - 75, Constants.BOARD_HEIGHT/2 - 25, 150, 50);

        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (Constants.BOARD_WIDTH - metr.stringWidth(message))/2, Constants.BOARD_WIDTH/2);
        g.drawString("Restart", (Constants.BOARD_WIDTH - metr.stringWidth("Restart"))/2, Constants.BOARD_HEIGHT/2+5);
    }

    public void animationCycle(){
        if(deaths == Constants.NUMBER_OF_ALIENS_TO_DESTROY){
            ingame = false;
            message = "Game won!";
        }

        if(server.playerBlinked() && System.currentTimeMillis() - lastShot >= Constants.SHOT_DELAY ){
            lastShot = System.currentTimeMillis();
            player.shoot();
        }

        //player
        player.act(server.getMoveDirection());

        int farthestLen = 0;

        for(Alien a : aliens){
            farthestLen = Math.max(farthestLen, a.getY());
        }

        farthestLen+=Constants.ALIEN_HEIGHT;

        //shots

        Iterator<Shot> it = player.shots.iterator();
        while(it.hasNext()) {

            Shot shot = it.next();

            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();

                if(shot.getY() <= farthestLen) {
                    for (Alien alien : aliens) {

                        int alienX = alien.getX();
                        int alienY = alien.getY();

                        if (alien.isVisible() && shot.isVisible()) {
                            if (shotX >= (alienX) &&
                                    shotX <= (alienX + Constants.ALIEN_WIDTH) &&
                                    shotY >= (alienY) &&
                                    shotY <= (alienY + Constants.ALIEN_HEIGHT)) {

                                ImageIcon ii =
                                        new ImageIcon(getClass().getResource(expl));
                                alien.setImage(ii.getImage().getScaledInstance(Constants.ALIEN_WIDTH, Constants.ALIEN_WIDTH, Image.SCALE_SMOOTH));
                                alien.setDying(true);
                                deaths++;
                                it.remove();
                                shot.die();
                            }
                        }
                    }
                }

                int y = shot.getY();
                y -= Constants.SHOT_TRAVEL_DIST;
                if (y < 0) {
                    it.remove();
                    shot.die();
                }
                else{
                    shot.setY(y);
                }
            }
        }

        //reverse aliens directions
        for (Alien alien1 : aliens) {
            
            int x = alien1.getX();

            if (x  >= Constants.BOARD_WIDTH - Constants.BORDER_RIGHT && direction != -1) {
                direction = -1;
                for (Alien alien2 : aliens) {
                    alien2.setY(alien2.getY() + Constants.GO_DOWN);
                }
            }

            if (x <= Constants.BORDER_LEFT && direction != 1) {
                direction = 1;

                for (Alien a : aliens) {
                    a.setY(a.getY() + Constants.GO_DOWN);
                }
            }
        }

        for (Alien alien: aliens) {
            if (alien.isVisible()) {

                int y = alien.getY();

                if (y > Constants.GROUND - Constants.ALIEN_HEIGHT) {
                    ingame = false;
                    message = "Invasion!";
                }

                alien.act(direction);
            }
        }

        // bombs

        Random generator = new Random();

        for(Alien a: aliens) {
            int shot = generator.nextInt(15);
            Alien.Bomb b = a.getBomb();
            if (shot == Constants.CHANCE && a.isVisible() && b.isDestroyed()) {

                b.setDestroyed(false);
                b.setX(a.getX());
                b.setY(a.getY());
            }

            int bombX = b.getX();
            int bombY = b.getY();
            int playerX = player.getX();
            int playerY = player.getY();

            if (player.isVisible() && !b.isDestroyed()) {
                if ( bombX >= (playerX) &&
                        bombX <= (playerX+Constants.PLAYER_WIDTH) &&
                        bombY >= (playerY) &&
                        bombY <= (playerY+Constants.PLAYER_HEIGHT) ) {
                    ImageIcon ii =
                            new ImageIcon(this.getClass().getResource(expl));
                    player.setImage(ii.getImage().getScaledInstance(Constants.ALIEN_WIDTH, Constants.ALIEN_WIDTH,Image.SCALE_SMOOTH));
                    player.setDying(true);
                    ingame = false;
                    b.setDestroyed(true);;
                }
            }

            if (!b.isDestroyed()) {
                b.setY(b.getY() + Constants.BOMB_TRAVEL_DIST);
                if (b.getY() >= Constants.GROUND - Constants.BOMB_HEIGHT) {
                    b.setDestroyed(true);
                }
            }
        }
    }

    public void run() {

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (ingame) {
            repaint();
            animationCycle();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = Constants.DELAY - timeDiff;

            if (sleep < 0)
                sleep = 2;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            beforeTime = System.currentTimeMillis();
        }
        gameOver();
    }

    private class TAdapter extends KeyAdapter implements MouseListener {

        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);

            if(ingame){

                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    spaceHeld = false;
                }
            }
        }

        public void keyPressed(KeyEvent e) {

            player.keyPressed(e);

            if (ingame)
            {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spaceHeld = true;
                }
            }
        }

        public void mouseClicked(MouseEvent e) {

            if(gameOver) {
                int posX = e.getX();
                int posY = e.getY();
                System.out.println(posX + ", " + posY);


                if (Constants.BOARD_WIDTH / 2 - 75 <= posX && Constants.BOARD_HEIGHT / 2 - 25 <= posY
                        && Constants.BOARD_WIDTH / 2 + 75 >= posX && Constants.BOARD_HEIGHT / 2 + 25 >= posY) {

                    System.out.println(" IN RECT");
                    gameInit();
                }
            }
        }

        public void mousePressed(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }
    }

}
