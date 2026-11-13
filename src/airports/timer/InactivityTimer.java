package airports.timer;

import airports.view.MainFrame;


import java.awt.EventQueue;

public class InactivityTimer extends Thread {

    private int secondsLeft = 60;
    private boolean running = true;

    private final MainFrame mainFrame;

    public InactivityTimer(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public synchronized void resetTimer() {
        secondsLeft = 60;
    }

    // ovo se poziva kad zatvaramo prozor
    public synchronized void stopTimer() {
        running = false;
        interrupt();
    }

    private synchronized boolean isRunning() {
        return running;
    }

    private synchronized int decreaseTime() {
        secondsLeft--;
        return secondsLeft;
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                if (!isRunning()) {
                    return;
                }
            }

            // ako je neki aerodrom selektovan, ne treba smanjivati odbrojavanje
            if (mainFrame.shouldPauseInactivityTimer() || mainFrame.isFileDialogOpen())
                continue;

            int currentSeconds = decreaseTime();
            // jedino nam zbog ovoga treba mainFrame polje
            EventQueue.invokeLater(() -> mainFrame.inactivityTimeChanged(currentSeconds));
            // ako vreme istekne, samo napustamo run metodu i to je to
            if (currentSeconds <= 0)
                return;

        }
    }
}