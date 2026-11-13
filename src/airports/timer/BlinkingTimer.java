package airports.timer;

import airports.view.AirportMapCanvas;

import java.awt.EventQueue;

public class BlinkingTimer extends Thread {

    private boolean running = true;  // da li ova nit treba da postoji uopste
    private final AirportMapCanvas airportMapCanvas;
    // treba nam zbog menjanja prozora i tako toga:
    private boolean active = false;

    public BlinkingTimer(AirportMapCanvas airportMapCanvas) {
        this.airportMapCanvas = airportMapCanvas;
    }

    public synchronized void stopTimer() {
        running = false;
        interrupt();
    }

    private synchronized boolean isRunning() {
        return running;
    }

    // kad prelazimo sa prozora na prozor, ovaj tajmer nam je neaktivan
    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    private synchronized boolean isActive() {
        return active;
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                if (!isRunning())
                    return;
            }
            if (isActive()) {
                EventQueue.invokeLater(() -> airportMapCanvas.changeBlinkingState());
            }
        }
    }
}