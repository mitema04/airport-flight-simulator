package airports.simulation;

import airports.model.Flight;

// klasa koja zapravo cuva podatke o letovima sa simulacije
public class SimulatedFlight {

    public enum Status {WAITING, FLYING, FINISHED}

    // svaki simulirani let cuva pokazivac na stvaran let koji simulira
    private final Flight flight;

    private final int scheduledDepartureMinute;
    private int actualDepartureMinute;
    private Status status;

    public SimulatedFlight(Flight flight) {
        this.flight = flight;
        this.scheduledDepartureMinute = flight.getDepartureTime().getHour() * 60 + flight.getDepartureTime().getMinute();
        this.actualDepartureMinute = -1;
        this.status = Status.WAITING;
    }

    public Flight getFlight() {
        return flight;
    }

    public int getScheduledDepartureMinute() {
        return scheduledDepartureMinute;
    }

    public int getActualDepartureMinute() {
        return actualDepartureMinute;
    }

    public void setActualDepartureMinute(int actualDepartureMinute) {
        this.actualDepartureMinute = actualDepartureMinute;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getArrivalMinute() {
        // ako nismo racunali pre ovo onda ni ne znamo ovo
        if (actualDepartureMinute == -1)
            return -1;
        return actualDepartureMinute + flight.getDurationMinutes();
    }

    // ovo nam treba da bismo izracunali koliki deo puta smo presli, pa na osnovu toga crtamo
    // koliko je procenata proslo od vremena, toliko je procenata proslo i od puta!
    public double calculateProgress(int currentSimulationMinute) {
        // ako ovo nismo racunali ranije, ne znamo
        if (actualDepartureMinute == -1)
            return 0.0;

        if (currentSimulationMinute <= actualDepartureMinute)
            return 0.0;

        if (currentSimulationMinute >= getArrivalMinute())
            return 1.0;

        int elapsedMinutes = currentSimulationMinute - actualDepartureMinute;
        return (double)elapsedMinutes / flight.getDurationMinutes();
    }

    public int getDelayMinutes() {
        if (actualDepartureMinute == -1)
            return 0;
        return Math.max(0, actualDepartureMinute - scheduledDepartureMinute);
    }
    public int getRemainingMinutes(int currentSimulationMinute) {
        if (status == Status.FINISHED)
            return 0;

        if (actualDepartureMinute == -1 || currentSimulationMinute < actualDepartureMinute)
            return flight.getDurationMinutes();

        return Math.max(0, getArrivalMinute() - currentSimulationMinute);
    }

}