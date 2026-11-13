package airports.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AirportSystem {

    private final List<Airport> airports;
    private final List<Flight> flights;

    public AirportSystem() {
        airports = new ArrayList<>();
        flights = new ArrayList<>();
    }

    private boolean flightExists(Flight flight) {
        for (Flight existingFlight : flights) {
            if (existingFlight.getDepartureAirport().equals(flight.getDepartureAirport())
                    && existingFlight.getDestinationAirport().equals(flight.getDestinationAirport())
                    && existingFlight.getDepartureTime().equals(flight.getDepartureTime())
                    && existingFlight.getDurationMinutes() == flight.getDurationMinutes()) {
                return true;
            }
        }
        return false;
    }


    public void addAirport(Airport airport) {
        if (airport==null) {
            throw new IllegalArgumentException("Pokusano dodavanje null");
        }

        if (findAirportByCode(airport.getCode())!=null) {
            throw new IllegalArgumentException("Aerodrom sa datim kodom vec postoji");
        }
        airports.add(airport);
    }

    public Airport findAirportByCode(String code) {
        if (code == null)
            return null;

        for (Airport airport : airports) {
            if (airport.getCode().equals(code.trim()))
                return airport;
        }
        return null;
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public void addFlight(Flight flight) {
        if (flight==null) {
            throw new IllegalArgumentException("Pokusano dodavanje null");
        }

        if (findAirportByCode(flight.getDepartureAirport().getCode())==null) {
            throw new IllegalArgumentException("Pocetni aerodrom ne postoji u sistemu");
        }

        if (findAirportByCode(flight.getDestinationAirport().getCode())==null) {
            throw new IllegalArgumentException("Krajnji aerodrom ne postoji u sistemu");
        }

        if (flightExists(flight)) {
            throw new IllegalArgumentException("Let sa identicnim podacima vec postoji");
        }
        flights.add(flight);
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void clearData() {
        airports.clear();
        flights.clear();
    }

    public List<Flight> findFlightsFrom(Airport airport) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights)
            if (flight.getDepartureAirport().equals(airport))
                result.add(flight);
        return result;
    }

    public List<Flight> findFlightsTo(Airport airport) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights)
            if (flight.getDestinationAirport().equals(airport))
                result.add(flight);
        return result;
    }

    public List<Flight> findFlightsFor(Airport airport) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights)
            if (flight.getDepartureAirport().equals(airport)
                    || flight.getDestinationAirport().equals(airport))
                result.add(flight);
        return result;
    }

    public boolean removeFlight(Flight flight) {
        return flights.remove(flight);
    }

    public boolean removeAirport(Airport airport) {
        if (airport == null)
            return false;

        flights.removeIf(flight ->
                flight.getDepartureAirport().equals(airport)
                        || flight.getDestinationAirport().equals(airport)
        );

        return airports.remove(airport);
    }
}
