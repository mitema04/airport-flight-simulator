package airports.timer;

import airports.simulation.Simulator;
import airports.view.SimulationPanel;

import java.awt.*;

public class SimulationTimer extends Thread {

    private boolean running = true;
    private final Simulator simulator;
    private final SimulationPanel simulationPanel;

    public SimulationTimer(Simulator simulator, SimulationPanel simulationPanel) {
        this.simulator = simulator;
        this.simulationPanel = simulationPanel;
    }

    public synchronized void stopTimer() {
        running = false;
        interrupt();
    }

    private synchronized boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException exception) {
                if (!isRunning()) {
                    return;
                }
            }

            // jer necemo dok je stopiran simulator da radimo nista
            if (!simulator.isRunning()) {
                continue;
            }

            // ovo je jako bitno zato sto gui nit treba da bude zaduzena za sve ove promene
            simulator.advanceTime();
            simulationPanel.updateDisplay();
            if (simulator.getCurrentSimulationMinute() % 10 == 0)
                EventQueue.invokeLater(() -> simulationPanel.updateTimeLabel());
        }
    }
}