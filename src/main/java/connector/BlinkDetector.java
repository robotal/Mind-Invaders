package connector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

public class BlinkDetector {

    public static final int LEFT_BLINK = 0;
    public static final int RIGHT_BLINK = 1;
    public static final int NORMAL_BLINK = 2;

    private static final double STD_DEV_FACTOR[] = {3.75,3,3,3.75};

    private LinkedList<EEGReading> readings = new LinkedList<>();
    private double sum[] = new double[4];
    private double sq_sum[] = new double[4];
    private double sigma[] = new double[4];
    private double isPeaking[] = new double[4];
    private final int SIGNAL_WINDOW = 1024;
    private int counts = 0 ;


    private final long BLINK_DELAY_TIME = 500;
    private final long BLINK_QUEUE_TIME = 100;
    private boolean blinkWaiting = false;
    private long blinkQueueTime = 0;
    private long blinkCompletedTime = 0;

    public void addEEGMessage(double vals[]){

        EEGReading nextReading = new EEGReading(vals);
        readings.add(nextReading);
        for(int i =0 ; i< 4 ;  i++){

            sum[i] += nextReading.eeg_vals[i];
            sq_sum[i]+= Math.pow(nextReading.eeg_vals[i],2);
        }

        long timeSinceLastBlink = System.currentTimeMillis() - blinkCompletedTime;

        if(counts >= SIGNAL_WINDOW){

            EEGReading oldReading = readings.remove();
            for(int i =0 ; i< 4 ; i++){

                sum[i] -= oldReading.eeg_vals[i];
                sq_sum[i] -= Math.pow(oldReading.eeg_vals[i],2);
                sigma[i] = Math.sqrt(sq_sum[i]/counts - Math.pow(sum[i]/counts,2));

                if(timeSinceLastBlink >= BLINK_DELAY_TIME) {
                    if (Math.abs(nextReading.eeg_vals[i] - sum[i] / counts) >
                            sigma[i] * STD_DEV_FACTOR[i]) {

                        System.out.println("EEG: "+i+" past mean by factor of " + (Math.abs(nextReading.eeg_vals[i] - sum[i] / counts)/sigma[i])+ " of std dev");

                        isPeaking[i] = System.currentTimeMillis();
                    }
                }
            }
        }
        else{
            counts++;
        }

        /**
         * eeg 1 - left eye
         * eeg 2 - right eye
         * 3 and 1 - present in both eyes, stronger when both
         */

        if(timeSinceLastBlink >= BLINK_DELAY_TIME) {
            long time = System.currentTimeMillis();

            // both eeg 0 and 3 are peaking
            if (time - isPeaking[0] <= BLINK_QUEUE_TIME && time - isPeaking[3] <= BLINK_QUEUE_TIME) {

                if (time - isPeaking[1] <= BLINK_QUEUE_TIME && time - isPeaking[2] <= BLINK_QUEUE_TIME) {
                    blinkWaiting = false;
                    blinkCompletedTime = System.currentTimeMillis();
                    System.out.println("Both eyes blinked");
                } else if (time - isPeaking[1] <= BLINK_QUEUE_TIME) {

                    if (!blinkWaiting) {

                        blinkQueueTime = System.currentTimeMillis();
                        blinkWaiting = true;
                    } else if (System.currentTimeMillis() - blinkQueueTime >= BLINK_QUEUE_TIME) {
                        blinkWaiting = false;
                        System.out.println("Left eye blinked");
                        blinkCompletedTime = System.currentTimeMillis();
                    }
                } else if (time - isPeaking[2] <= BLINK_QUEUE_TIME) {

                    if (!blinkWaiting) {

                        blinkQueueTime = System.currentTimeMillis();
                        blinkWaiting = true;
                    } else if (System.currentTimeMillis() - blinkQueueTime >= BLINK_QUEUE_TIME) {
                        blinkWaiting = false;
                        System.out.println("Right eye blinked");
                        blinkCompletedTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    private class BlinkAction implements Action {

        @Override
        public Object getValue(String key) {
            return null;
        }

        @Override
        public void putValue(String key, Object value) {

        }

        @Override
        public void setEnabled(boolean b) {

        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {

        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {

        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    private class EEGReading{

        double eeg_vals[];

        long timestamp;

        public EEGReading(double vals[]){

            eeg_vals = vals;
            timestamp = System.currentTimeMillis();
        }
    }
}
