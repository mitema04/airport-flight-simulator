package airports.view;

import airports.model.AirportSystem;
import airports.model.Flight;
import airports.simulation.Simulator;
import airports.timer.SimulationTimer;

import java.awt.*;

public class SimulationPanel extends Panel {

    private final AirportSystem airportSystem;
    private final Simulator simulator;
    private final Label timeLabel;
    private final SimulationTimer simulationTimer;
    private final Label statusLabel;
    private final SimulationCanvas simulationCanvas;

    public SimulationPanel(AirportSystem airportSystem) {
        this.airportSystem = airportSystem;
        this.simulator = new Simulator();
        // damo simulatoru sve letove, on nema referencu na airportSystem, samo ima listu letova
        for (Flight flight : airportSystem.getFlights()) {
            simulator.addFlight(flight);
        }
        simulator.prepareSimulation();
        simulationTimer = new SimulationTimer(simulator, this);
        simulationTimer.start();

        setLayout(new BorderLayout());
        simulationCanvas = new SimulationCanvas(airportSystem, simulator);
        add(simulationCanvas, BorderLayout.CENTER);
        timeLabel = new Label("Vreme simulacije: 00:00", Label.CENTER);
        add(timeLabel, BorderLayout.NORTH);
        Panel southPanel = new Panel(new BorderLayout());
        Panel buttonPanel = new Panel();
        Button startButton = new Button("Pokreni");
        Button pauseButton = new Button("Pauziraj");
        Button resetButton = new Button("Resetuj");

        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);

        statusLabel = new Label("");
        statusLabel.setPreferredSize(new Dimension(120, 25));

        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);


        startButton.addActionListener(event -> {
            // ako je ovo stvarno start a ne resume, onda ovaj if
            // reload flights u sebi pozove i prepareSimulation
            if (simulator.getCurrentSimulationMinute() == 0)
                reloadFlights();
            simulator.start();
            statusLabel.setText("Simulacija u toku");
        });

        pauseButton.addActionListener(event -> {
            statusLabel.setText("Simulacija pauzirana");
            simulator.pause();
        });

        resetButton.addActionListener(event -> {
            simulator.reset();
            // ovo update-uje prikaz platna
            updateDisplay();
            // jer na reset dugmeta treba i da se odmah promeni labela
            updateTimeLabel();
            statusLabel.setText("");
        });
    }

    private String formatTime(int totalMinutes) {
        int hours = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public void updateTimeLabel() {
        int currentMinute = simulator.getCurrentSimulationMinute();
        String formattedTime = formatTime(currentMinute);
        timeLabel.setText("Vreme simulacije: " + formattedTime);
    }

    // OVA funkcija zapravo poziva repaint i updatuje kako stanje na platnu tako i opis simulacije
    public void updateDisplay() {
        simulationCanvas.repaint();
        if (simulator.isFinished()) {
            statusLabel.setText("Simulacija zavrsena");
        }
    }

    // ovo nam treba jer se prazna lista letova pravi odmah pri ucitavanju aplikacije
    // a mi treba da uhvatimo i trenutak kad su letovi stvarno ucitani, ili kad su dodati i tako to
    private void reloadFlights() {
        simulator.clearFlights();
        for (Flight flight : airportSystem.getFlights()) {
            simulator.addFlight(flight);
        }
        simulator.prepareSimulation();
    }

    public boolean isSimulationRunning() {
        return simulator.isRunning();
    }

    public void stopSimulationTimer() {
        simulationTimer.stopTimer();
    }
}