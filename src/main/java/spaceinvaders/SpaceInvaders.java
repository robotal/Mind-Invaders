package spaceinvaders;

import connector.MuseOscServer;

import javax.swing.JFrame;
import java.io.IOException;

public class SpaceInvaders extends JFrame {

    public SpaceInvaders(MuseOscServer server){

        super();

        add(new Board(server));
        setTitle("Mind Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        toFront();
   }

    public static void main(String[] args){

        System.out.println("Press the enter key to calibrate the accelerometer and begin the game...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MuseOscServer server = new MuseOscServer();
        server.calibrate();

        SpaceInvaders game = new SpaceInvaders(server);
    }
}
