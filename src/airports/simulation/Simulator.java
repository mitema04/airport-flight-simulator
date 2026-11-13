package airports.simulation;

import airports.model.Airport;
import airports.model.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// klasa koja se bavi time da rasporedi letove i rukuje njihovim statusima
// koristi je direktno simulationPanel
public class Simulator {

    // cuvamo koliko je minuta proslo od pocetka simulacije ukupno, nije ograniceno na 24*60
    private int currentSimulationMinute;

    // trebace nam i svi letovi koje treba da simuliramo
    private final List<SimulatedFlight> simulatedFlights;

    // za svaki aerodrom ovo nam cuva prvi slobodan termin u minutima kad moze neki avion da poleti
    private final Map<Airport, Integer> nextFreeDepartureMinute;

    // da li je simulacija u toku
    private boolean running;

    // ovo nam je trebalo narocito za ispis
    private boolean finished;

    public Simulator() {
        currentSimulationMinute = 0;
        simulatedFlights = new ArrayList<>();
        nextFreeDepartureMinute = new HashMap<>();
        running = false;
        finished = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentSimulationMinute() {
        return currentSimulationMinute;
    }

    public List<SimulatedFlight> getSimulatedFlights() {
        return simulatedFlights;
    }

    // prosledimo obican let, a on napravi objekat simuliranog leta
    public void addFlight(Flight flight) {
        simulatedFlights.add(new SimulatedFlight(flight));
    }

    // azuriranje statusa simuliranih letova
    public void updateFlights() {
        for (SimulatedFlight simulatedFlight : simulatedFlights) {
            if (simulatedFlight.getActualDepartureMinute() == -1)
                continue;
            if (currentSimulationMinute >= simulatedFlight.getArrivalMinute())
                simulatedFlight.setStatus(SimulatedFlight.Status.FINISHED);
            else if (currentSimulationMinute >= simulatedFlight.getActualDepartureMinute())
                simulatedFlight.setStatus(SimulatedFlight.Status.FLYING);

        }
    }

    // zato sto treba da se azurira na 200ms polozaj aviona, a to je na nasem siumaltoru 2 minuta
    // ova fja se zove iz simulation timera stalno, a ako simulator radi, samo ce onda da azurira minute
    public void advanceTime() {
        if (!running)
            return;
        // azuriramo protekle minute
        currentSimulationMinute += 2;
        // azuriramo statuse letova
        updateFlights();
        // i ako su svi zavrseni, zavrsavamo simulaciju
        if (areAllFlightsFinished()) {
            finished = true;
            running = false;
        }
    }


    private int roundUpToTenMinutes(int minutes) {
        int left = minutes % 10;
        if (left == 0)
            return minutes;
        return minutes + (10 - left);
    }

    // ovo nam je najbitnija funkcija ovde, koja zapravo namesta kada koji avion treba da poleti
    public void scheduleDepartures() {
        // ovo nam je trebalo da za svaki slucaj pre preracunavanja izbrisemo sve
        // iz hashmape
        nextFreeDepartureMinute.clear();
        // svakako prvo sortiramo po vremenima, da bi npr avion u 10:33 poleteo pre aviona u 10:35
        simulatedFlights.sort(
                (first, second) -> Integer.compare(
                        first.getScheduledDepartureMinute(),
                        second.getScheduledDepartureMinute()
                )
        );

        for (SimulatedFlight simulatedFlight : simulatedFlights) {
            int scheduledMinute = simulatedFlight.getScheduledDepartureMinute();
            int departureMinute = roundUpToTenMinutes(scheduledMinute);

            Airport departureAirport = simulatedFlight.getFlight().getDepartureAirport();

            // ako u hashmap imamo vec upisn slobodan termin, znaci mozda pomeramo nase poletanje
            if (nextFreeDepartureMinute.containsKey(departureAirport)) {
                int nextFreeMinute = nextFreeDepartureMinute.get(departureAirport);
                if (departureMinute < nextFreeMinute)
                    departureMinute = nextFreeMinute;
            }

            simulatedFlight.setActualDepartureMinute(departureMinute);
            // stavljamo da je 10 minuta kasnije od naseg poletanja sledeci slobodan termin
            nextFreeDepartureMinute.put(departureAirport, departureMinute + 10);
        }
    }

    public void prepareSimulation() {
        running = false;
        finished = false;
        currentSimulationMinute = 0;
        for (SimulatedFlight simulatedFlight : simulatedFlights)
            simulatedFlight.setStatus(SimulatedFlight.Status.WAITING);
        // uvek kada zapocinjemo simulaciju iznova ovo radimo, jer su mozda neki letovi u medjuvremenu dodati
        scheduleDepartures();
    }

    // ovo je isto sto bi se zvalo i za resume, nema posebne fje resume, ne treba nam
    public void start() {
        running = true;
    }

    public void pause() {
        running = false;
    }

    public void reset(){
        prepareSimulation();
    }

    public void clearFlights() {
        simulatedFlights.clear();
    }

    public boolean isFinished(){
        return finished;
    }

    private boolean areAllFlightsFinished() {
        for (SimulatedFlight simulatedFlight : simulatedFlights) {
            if (simulatedFlight.getStatus() != SimulatedFlight.Status.FINISHED)
                return false;
        }
        return true;
    }

    public List<SimulatedFlight> getFlightsByStatus(SimulatedFlight.Status status) {
        List<SimulatedFlight> result = new ArrayList<>();
        for (SimulatedFlight flight : simulatedFlights)
            if (flight.getStatus() == status)
                result.add(flight);
        return result;
    }

}
