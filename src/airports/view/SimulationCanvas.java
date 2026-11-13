package airports.view;

import airports.model.Airport;
import airports.model.AirportSystem;
import airports.simulation.SimulatedFlight;
import airports.simulation.Simulator;

import java.awt.*;


public class SimulationCanvas extends Canvas {
    private static final int MAP_MARGIN = 30;
    private static final int AIRPORT_SIZE = 10;
    private static final int AIRPLANE_SIZE = 6;

    // treba nam ovaj sistem da bismo dohvatili sve aerodrome
    private final AirportSystem airportSystem;

    // dok preko simulatora dohvatamo SimulatedFLights koji nam ovde trebaju, kao i metodu za kalkulaciju progresa
    private final Simulator simulator;

    public SimulationCanvas(AirportSystem airportSystem, Simulator simulator) {
        this.airportSystem = airportSystem;
        this.simulator = simulator;
        setBackground(new Color(190, 225, 255));
    }

    // koja je zapravo x koordinata na platnu
    private int getScreenX(Airport airport) {
        return MAP_MARGIN + (int) Math.round((airport.getX() + 180) * (getWidth() - 2 * MAP_MARGIN) / 360.0);
    }

    // koja je zapravo y koordinata na platnu
    private int getScreenY(Airport airport) {
        return MAP_MARGIN + (int) Math.round((90 - airport.getY()) * (getHeight() - 2 * MAP_MARGIN) / 180.0);
    }

    @Override
    public void paint(Graphics graphics) {
        for (Airport airport : airportSystem.getAirports()) {
            // ovaj if je ono za checkboxove, oni aerodromi koje nismo selektovali ne treba da se prikazuju
            if (!airport.isVisible()) {
                continue;
            }
            int screenX = getScreenX(airport);
            int screenY = getScreenY(airport);

            graphics.setColor(Color.GRAY);
            graphics.fillRect(screenX - AIRPORT_SIZE / 2, screenY - AIRPORT_SIZE / 2, AIRPORT_SIZE, AIRPORT_SIZE);

            graphics.setColor(Color.BLACK);
            graphics.drawString(airport.getCode(), screenX + 2, screenY - 7);
        }

        for (SimulatedFlight simulatedFlight : simulator.getSimulatedFlights()) {
            // ako let nije u stanju letenja, ne treba jos nista sa njim da radimo
            if (simulatedFlight.getStatus() != SimulatedFlight.Status.FLYING)
                continue;

            Airport departureAirport = simulatedFlight.getFlight().getDepartureAirport();
            Airport destinationAirport = simulatedFlight.getFlight().getDestinationAirport();

            // ovaj if je da se ne prikazuju letovi za koje neki aerodrom nije prikazan
            if (!departureAirport.isVisible() || !destinationAirport.isVisible())
                continue;

            int startX = getScreenX(departureAirport);
            int startY = getScreenY(departureAirport);
            int endX = getScreenX(destinationAirport);
            int endY = getScreenY(destinationAirport);

            graphics.setColor(Color.RED);
            graphics.drawLine(startX, startY, endX, endY);

            double progress = simulatedFlight.calculateProgress(simulator.getCurrentSimulationMinute());
            int airplaneX = startX + (int) Math.round((endX - startX) * progress);
            int airplaneY = startY + (int) Math.round((endY - startY) * progress);

            graphics.setColor(Color.BLUE);
            graphics.fillOval(airplaneX - AIRPLANE_SIZE / 2, airplaneY - AIRPLANE_SIZE / 2, AIRPLANE_SIZE, AIRPLANE_SIZE);
        }

    }

}
