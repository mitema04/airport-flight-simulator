package airports.model;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class Flight {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Airport departureAirport;
    private final Airport destinationAirport;
    private final LocalTime departureTime;
    private final int durationMinutes;

    public Flight(Airport departureAirport, Airport destinationAirport, LocalTime departureTime, int durationMinutes) {
        if (departureAirport==null) {
            throw new IllegalArgumentException("Pocetni aerodrom mora da bude zadat");
        }

        if (destinationAirport == null) {
            throw new IllegalArgumentException("Krajnji aerodrom mora da bude zadat");
        }

        if (departureAirport.equals(destinationAirport)) {
            throw new IllegalArgumentException("Pocetni i krajnji aerodrom ne mogu da budu isti");
        }

        if (departureTime==null) {
            throw new IllegalArgumentException("Vreme poletanja mora da bude zadato");
        }

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Trajanje leta mora da bude vece od nule");
        }

        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.durationMinutes = durationMinutes;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getDestinationAirport() {
        return destinationAirport;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalTime getArrivalTime() {
        return departureTime.plusMinutes(durationMinutes);
    }

    @Override
    public String toString() {
        return departureAirport.getCode() + " -> " + destinationAirport.getCode()
                + " | " + departureTime.format(TIME_FORMAT) + " | " + durationMinutes + "min";
    }
}
