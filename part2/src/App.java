import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class App {
    static final double TIME_SCALE = 1.0 / (60);
    // static final double TIME_SCALE = 0.1;
    static final int NUM_HOURS = 10;
    static final int NUM_READS = NUM_HOURS * 60;
    static final int NUM_THREADS = 8;
    static final int SUMMARY_KEEP = 5;
    
    
    static class TemperatureSensor extends Thread {
        volatile double last_temp;
        volatile boolean should_stop = false;

        @Override
        public void run() {
            // seed a new random number generator
            Random r = new Random();
            // get thread id
            long id = Thread.currentThread().getId();
            r.setSeed(id);
            while (!should_stop) {
                // generate 'random' temperature from -100 to 70
                last_temp = -100 + (Math.random() * (70 + 100));
                // wait 1*TIME_SCALE seconds
                try {
                    Thread.sleep((long) (TIME_SCALE * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        ArrayList<ArrayList<Double>> temps = new ArrayList<>();
        ArrayList<Double> highests = new ArrayList<>();
        ArrayList<Double> lowests = new ArrayList<>();
        ArrayList<Double> rollingBuffer = new ArrayList<>();
        double maxdiff = 0;
        int maxdiff_time = 0;
        System.out.println("using time scale: " + TIME_SCALE);
        for (int i = 0; i < NUM_THREADS; i++) {
            ArrayList<Double> temp = new ArrayList<>();
            temps.add(temp);
        }
        ArrayList<TemperatureSensor> sensors = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            TemperatureSensor sensor = new TemperatureSensor();
            sensors.add(sensor);
        }
        for (TemperatureSensor sensor : sensors) {
            sensor.start();
        }
        int runs = 0;
        while (runs < NUM_READS) {
            double avgtmp = 0;
            for (int i = 0; i < NUM_THREADS; i++) {
                double temp = sensors.get(i).last_temp;
                avgtmp += temp;
                temps.get(i).add(temp);

                if (highests.size() == 0 || temp > highests.get(0)) {
                    highests.add(temp);
                    Collections.sort(highests);
                }
                if (lowests.size() == 0 || temp < lowests.get(lowests.size() - 1)) {
                    lowests.add(temp);
                    Collections.sort(lowests);
                }
                if (highests.size() > SUMMARY_KEEP) {
                    highests.remove(0);
                }
                if (lowests.size() > SUMMARY_KEEP) {
                    lowests.remove(lowests.size() - 1);
                }
            }
            avgtmp /= NUM_THREADS;
            rollingBuffer.add(avgtmp);
            if (rollingBuffer.size() > 10) {
                rollingBuffer.remove(0);
            }
            if (rollingBuffer.size() == 10) {
                double diff = Math.abs(rollingBuffer.get(0)- rollingBuffer.get(9));
                if (diff > maxdiff) {
                    maxdiff = diff;
                    maxdiff_time = runs;
                }
            }
            runs++;
            // sleep for 1*TIME_SCALE seconds
            // did an hour pass?
            if (runs != 0 && runs % 60 == 0) {
                // print the highests and lowests
                System.out.println("highests: " + highests);
                System.out.println("lowests: " + lowests);
                System.out.println("Largest 10 min diff: " + maxdiff + " at " + maxdiff_time);
                // clear the highests and lowests
                highests.clear();
                lowests.clear();
                rollingBuffer.clear();
                maxdiff = 0;
            }
            try {
                Thread.sleep((long) (TIME_SCALE * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            for (int i = 0; i < NUM_THREADS; i++) {
                sensors.get(i).should_stop = true;
            }
    }
}
