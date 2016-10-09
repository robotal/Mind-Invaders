package connector;

import oscP5.*;
import spaceinvaders.Constants;

import javax.swing.*;
import java.awt.*;

public class MuseOscServer {

    //private static AccelerationFrame aFrame;

    OscP5 museServer;
    //private BlinkDetector blinkDetector;
    int recvPort = 5000;

    double calibratedValues[] = new double[3];
    private boolean readyToCalibrate = false;

    private double acceleration[] = new double[3];
    private boolean blinked = false;

    public MuseOscServer(){
        //aFrame = new AccelerationFrame();
        museServer = new OscP5(this, recvPort);
        //blinkDetector = new BlinkDetector();
    }


    void oscEvent(OscMessage msg) {
        /*
        if (msg.checkAddrPattern("/muse/eeg")) {

            double eegVals[]  = new double[4];
            for(int i = 0; i < 4; i++) {
                eegVals[i] =  msg.get(i).floatValue();
            }

             blinkDetector.addEEGMessage(eegVals);
        }
        */
        if (msg.checkAddrPattern("/muse/acc")) {
            for (int i = 0; i < 3; i++) {
                acceleration[i] = msg.get(i).floatValue();
            }

            if(readyToCalibrate){
                for(int i = 0; i< 3; i++){
                    calibratedValues[i] = acceleration[i];
                }
                readyToCalibrate = false;
            }
            //aFrame.updateAccValues(acceleration);
        }
        else if(msg.checkAddrPattern("/muse/elements/blink")){

            int blink = msg.get(0).intValue();

            if(blink == 1){
                blinked = true;
                System.out.println("BLINKED");
            }

        }
    }

     /*

        Positive change in X indicates forward motion (hangs around 100)
        Positive change in Y inicates moving up and down (hangs around 980 but irrelevant)
        Positive change in Z indicates leaning to RIGHT
     */
    /**
     * Returns a float array of the direction vector of the acceleremoter based on calibrated values
     * @return
     */
    public double[] getMoveDirection (){
        double dir[] = new double[2];

        dir[0] = (acceleration[2]- calibratedValues[2]);
        dir[1] = (acceleration[0]- calibratedValues[0]);

        dir[0]/=300.0;
        dir[1]/=-300.0;

        //System.out.println("ACC diff: " + dir[0] + ", " + dir[1]);

        dir[0] = Math.max(-1, dir[0]);
        dir[0] = Math.min(1, dir[0]);
        dir[1] = Math.max(-1, dir[1]);
        dir[1] = Math.min(1, dir[1]);

        return dir;
    }


    public boolean playerBlinked(){

        boolean toRet = blinked;
        blinked = false;
        return toRet;
    }

    public void calibrate(){
        readyToCalibrate = true;
    }

    private static class AccelerationPanel extends JPanel {

        private Dimension d;
        private JLabel[] fields;

        public AccelerationPanel() {

            d = new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
            setBackground(Color.black);

            fields = new JLabel[3];

            /*

            Positive change in X indicates forward motion (hangs around 100)
            Positive change in Y inicates moving up and down (hangs around 980 but irrelevant)
            Positive change in Z indicates leaning to RIGHT
             */
            fields[0] = new JLabel("X: 0");
            fields[1] = new JLabel("Y: 0");
            fields[2] = new JLabel("Z: 0");

            for(JLabel jl: fields){
                add(jl);
            }

            setVisible(true);
        }

        public void paint(Graphics g) {

            super.paint(g);
            g.setColor(Color.black);
        }

        public void updateAccValues(float current[]){

            fields[0].setText("X: " + current[0]);
            fields[1].setText("Y: " + current[1]);
            fields[2].setText("Z: " + current[2]);
        }
    }

    private static class AccelerationFrame extends JFrame {

        private AccelerationPanel aPanel;

        public void updateAccValues(float current[]){
            aPanel.updateAccValues(current);
        }

        public AccelerationFrame() {

            aPanel = new AccelerationPanel();
            add(aPanel);
            setTitle("Acceleration Graphic");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
            setLocationRelativeTo(null);
            setVisible(true);
            setResizable(false);
        }

    }


    public static void main(String[] args) {

        MuseOscServer server = new MuseOscServer();
    }

}
