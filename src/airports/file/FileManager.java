package airports.file;

import airports.model.Airport;
import airports.model.AirportSystem;
import airports.model.Flight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileManager {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static class JsonData {
        private AirportJson[] airports;
        private FlightJson[] flights;
    }

    private static class AirportJson {
        private String code;
        private String name;
        private int x;
        private int y;
    }

    private static class FlightJson {
        private String from;
        private String to;
        private String departure;
        private int duration;
    }


    public static void loadCsv(File file, AirportSystem airportSystem) {
        if (file == null) {
            throw new IllegalArgumentException("Fajl nije izabran");
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Izabrani fajl mora da bude u CSV formatu");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            airportSystem.clearData();
            String section = "";
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equals("# AIRPORTS")) {
                    section = "AIRPORTS";
                    continue;
                }

                if (line.equals("# FLIGHTS")) {
                    section = "FLIGHTS";
                    continue;
                }

                if (line.equals("CODE,NAME,X,Y") || line.equals("FROM,TO,DEPARTURE,DURATION"))  {
                    continue;
                }

                try {
                    if (section.equals("AIRPORTS")) {
                        loadCSVAirport(line, airportSystem);
                    } else if (section.equals("FLIGHTS")) {
                        loadCSVFlight(line, airportSystem);
                    } else {
                        throw new IllegalArgumentException("Nedostaje oznaka sekcije");
                    }
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Greska u redu " + lineNumber + ": " + exception.getMessage());
                }
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne postoji ili ne moze da se procita");
        }
    }

    public static void loadJson(File file, AirportSystem airportSystem){
        loadJsonGson(file, airportSystem);
    }

    public static void loadJsonManual(File file, AirportSystem airportSystem) {
        if (file == null) {
            throw new IllegalArgumentException("Fajl nije izabran");
        }

        if (!file.getName().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Izabrani fajl mora biti u JSON formatu");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            airportSystem.clearData();
            String section = "";
            String line;
            boolean airportsFound = false;
            boolean flightsFound = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.equals("{") || line.equals("}") || line.equals("],") || line.equals("]")) {
                    continue;
                }
                if (line.startsWith("\"airports\"")) {
                    section = "AIRPORTS";
                    airportsFound = true;
                    continue;
                }

                if (line.startsWith("\"flights\"")) {
                    section = "FLIGHTS";
                    flightsFound = true;
                    continue;
                }

                if (line.startsWith("{")) {
                    if (section.equals("AIRPORTS")) {
                        loadJsonAirport(line, airportSystem);
                    } else if (section.equals("FLIGHTS")) {
                        loadJsonFlight(line, airportSystem);
                    } else {
                        throw new IllegalArgumentException("JSON fajl nema ispravan format");
                    }
                }
            }

            if (!airportsFound || !flightsFound) {
                throw new IllegalArgumentException("JSON fajl nema ispravan format");
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne postoji ili ne moze da se procita");

        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new IllegalArgumentException("JSON fajl nema ispravan format.");
        }
    }


    public static void loadJsonGson(File file, AirportSystem airportSystem) {
        if (file == null) {
            throw new IllegalArgumentException("Fajl nije izabran");
        }

        if (!file.getName().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Izabrani fajl mora biti u JSON formatu");
        }
        Gson gson = new Gson();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonData jsonData = gson.fromJson(reader, JsonData.class);
            if (jsonData == null || jsonData.airports == null || jsonData.flights == null) {
                throw new IllegalArgumentException("JSON fajl nema ispravan format");
            }

            // ovo se pravi da ne ostanemo sa polovicno ucitanim podacima, pa pravimo kopiju za slucaj gresaka
            AirportSystem loadedSystem = new AirportSystem();

            for (AirportJson airportJson : jsonData.airports) {
                Airport airport = new Airport(airportJson.name, airportJson.code, airportJson.x, airportJson.y);
                loadedSystem.addAirport(airport);
            }

            for (FlightJson flightJson : jsonData.flights) {
                Airport departureAirport = loadedSystem.findAirportByCode(flightJson.from);
                Airport destinationAirport = loadedSystem.findAirportByCode(flightJson.to);
                if (departureAirport == null) {
                    throw new IllegalArgumentException("Pocetni aerodrom " + flightJson.from + " ne postoji");
                }
                if (destinationAirport == null) {
                    throw new IllegalArgumentException("Krajnji aerodrom " + flightJson.to + " ne postoji");
                }
                LocalTime departureTime = LocalTime.parse(flightJson.departure, TIME_FORMAT);
                Flight flight = new Flight(departureAirport, destinationAirport, departureTime, flightJson.duration);
                loadedSystem.addFlight(flight);
            }

            airportSystem.clearData();
            for (Airport airport : loadedSystem.getAirports()) {
                airportSystem.addAirport(airport);
            }
            for (Flight flight : loadedSystem.getFlights()) {
                airportSystem.addFlight(flight);
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne postoji ili ne moze da se procita");

        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Vreme poletanja mora da bude u formatu HH:mm");
        }
    }


    // za manuelno ucitavanje
    private static String[] getJsonParts(String line) {
        // sklanjamo zarez sa kraja
        if (line.endsWith(",")) {
            line = line.substring(0, line.length() - 1);
        }

        // sklanjamo pocetnu i krajnju zagradu
        if (!line.startsWith("{") || !line.endsWith("}")) {
            throw new IllegalArgumentException("JSON fajl nema ispravan format");
        }

        line = line.substring(1, line.length() - 1);
        // uklanjamo navodnike
        line = line.replace("\"", "");
        String[] parts = line.split(",");

        if (parts.length != 4) {
            throw new IllegalArgumentException("JSON fajl nema ispravan format");
        }
        return parts;
    }

    // za manuelno ucitavanje
    private static String getJsonValue(String part) {
        String[] pair = part.split(":", 2);
        if (pair.length != 2) {
            throw new IllegalArgumentException("JSON fajl nema ispravan format.");
        }
        return pair[1].trim();
    }

    // za manuelno ucitavanje
    private static void loadJsonAirport(String line, AirportSystem airportSystem) {
        String[] parts = getJsonParts(line);
        String code = getJsonValue(parts[0]);
        String name = getJsonValue(parts[1]);
        int x = Integer.parseInt(getJsonValue(parts[2]));
        int y = Integer.parseInt(getJsonValue(parts[3]));
        Airport airport = new Airport(name, code, x, y);
        airportSystem.addAirport(airport);
    }

    // za manuelno ucitavanje
    private static void loadJsonFlight(String line, AirportSystem airportSystem) {
        String[] parts = getJsonParts(line);
        String departureCode = getJsonValue(parts[0]);
        String destinationCode = getJsonValue(parts[1]);
        Airport departureAirport = airportSystem.findAirportByCode(departureCode);
        Airport destinationAirport = airportSystem.findAirportByCode(destinationCode);

        if (departureAirport == null) {
            throw new IllegalArgumentException("Pocetni aerodrom " + departureCode + " ne postoji");
        }

        if (destinationAirport == null) {
            throw new IllegalArgumentException("Krajnji aerodrom " + destinationCode + " ne postoji");
        }

        LocalTime departureTime = LocalTime.parse(getJsonValue(parts[2]), TIME_FORMAT);
        int durationMinutes = Integer.parseInt(getJsonValue(parts[3]));
        Flight flight = new Flight(departureAirport, destinationAirport, departureTime, durationMinutes);
        airportSystem.addFlight(flight);
    }

    public static void saveJson(File file, AirportSystem airportSystem) {
        saveJsonGson(file, airportSystem);
    }

    public static void saveJsonManual(File file, AirportSystem airportSystem) {
        if (file == null)
            throw new IllegalArgumentException("Fajl nije izabran");

        if (!file.getName().toLowerCase().endsWith(".json"))
            throw new IllegalArgumentException("Fajl mora biti sacuvan u JSON formatu");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("{");
            writer.newLine();
            writer.write("\"airports\":[");
            writer.newLine();

            for (int i = 0; i < airportSystem.getAirports().size(); i++) {
                Airport airport = airportSystem.getAirports().get(i);
                writer.write(
                        "{\"code\":\"" + airport.getCode()
                                + "\",\"name\":\"" + airport.getName()
                                + "\",\"x\":" + airport.getX()
                                + ",\"y\":" + airport.getY()
                                + "}"
                );
                if (i < airportSystem.getAirports().size() - 1)
                    writer.write(",");
                writer.newLine();
            }

            writer.write("],");
            writer.newLine();
            writer.write("\"flights\":[");
            writer.newLine();
            for (int i = 0; i < airportSystem.getFlights().size(); i++) {
                Flight flight = airportSystem.getFlights().get(i);
                writer.write(
                        "{\"from\":\"" + flight.getDepartureAirport().getCode()
                                + "\",\"to\":\"" + flight.getDestinationAirport().getCode()
                                + "\",\"departure\":\"" + flight.getDepartureTime().format(TIME_FORMAT)
                                + "\",\"duration\":" + flight.getDurationMinutes()
                                + "}"
                );
                if (i < airportSystem.getFlights().size() - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }

            writer.write("]");
            writer.newLine();
            writer.write("}");

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne moze da se sacuva");
        }
    }

    public static void saveJsonGson(File file, AirportSystem airportSystem) {
        if (file == null) {
            throw new IllegalArgumentException("Fajl nije izabran");
        }

        if (!file.getName().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Fajl mora biti sacuvan u JSON formatu");
        }
        JsonData jsonData = new JsonData();
        jsonData.airports = new AirportJson[airportSystem.getAirports().size()];
        for (int i = 0; i < airportSystem.getAirports().size(); i++) {
            Airport airport = airportSystem.getAirports().get(i);
            AirportJson airportJson = new AirportJson();
            airportJson.code = airport.getCode();
            airportJson.name = airport.getName();
            airportJson.x = airport.getX();
            airportJson.y = airport.getY();
            jsonData.airports[i] = airportJson;
        }

        jsonData.flights = new FlightJson[airportSystem.getFlights().size()];

        for (int i = 0; i < airportSystem.getFlights().size(); i++) {
            Flight flight = airportSystem.getFlights().get(i);
            FlightJson flightJson = new FlightJson();
            flightJson.from = flight.getDepartureAirport().getCode();
            flightJson.to = flight.getDestinationAirport().getCode();
            flightJson.departure = flight.getDepartureTime().format(TIME_FORMAT);
            flightJson.duration = flight.getDurationMinutes();
            jsonData.flights[i] = flightJson;
        }

        // ovo je da nam ne bi bilo sve u jednom redu ruzno ispisano
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(jsonData, writer);

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne moze da se sacuva");
        }
    }

    private static void loadCSVAirport(String line, AirportSystem airportSystem) {
        String[] parts = line.split(",", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Aerodrom mora imati kod, naziv, X i Y koordinatu");
        }

        try {
            String code = parts[0].trim();
            String name = parts[1].trim();
            int x = Integer.parseInt(parts[2].trim());
            int y = Integer.parseInt(parts[3].trim());

            Airport airport = new Airport(name, code, x, y);
            airportSystem.addAirport(airport);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Koordinate aerodroma moraju biti celi brojevi");
        }
    }

    private static void loadCSVFlight(String line, AirportSystem airportSystem) {
        String[] parts = line.split(",", 4);

        if (parts.length != 4) {
            throw new IllegalArgumentException("Let mora imati pocetni aerodrom, krajnji aerodrom, vreme i trajanje");
        }

        String departureCode = parts[0].trim();
        String destinationCode = parts[1].trim();

        Airport departureAirport = airportSystem.findAirportByCode(departureCode);

        Airport destinationAirport = airportSystem.findAirportByCode(destinationCode);

        if (departureAirport == null) {
            throw new IllegalArgumentException("Pocetni aerodrom " + departureCode + " ne postoji");
        }

        if (destinationAirport == null) {
            throw new IllegalArgumentException("Krajnji aerodrom " + destinationCode + " ne postoji");
        }

        LocalTime departureTime;

        try {
            departureTime = LocalTime.parse(parts[2].trim(), TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Vreme poletanja mora biti u formatu HH:mm");
        }

        int durationMinutes;

        try {
            durationMinutes = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Trajanje leta mora da bude ceo broj minuta");
        }

        Flight flight = new Flight(departureAirport, destinationAirport, departureTime, durationMinutes);
        airportSystem.addFlight(flight);
    }

    public static void saveCsv(File file, AirportSystem airportSystem) {
        if (file == null) {
            throw new IllegalArgumentException("Fajl nije izabran");
        }

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Fajl mora biti sacuvan u CSV formatu");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# AIRPORTS");
            writer.newLine();
            writer.write("CODE,NAME,X,Y");
            writer.newLine();

            for (Airport airport : airportSystem.getAirports()) {
                writer.write(airport.getCode() + "," + airport.getName() + "," + airport.getX() + "," + airport.getY());
                writer.newLine();
            }

            writer.newLine();
            writer.write("# FLIGHTS");
            writer.newLine();
            writer.write("FROM,TO,DEPARTURE,DURATION");
            writer.newLine();

            for (Flight flight : airportSystem.getFlights()) {
                writer.write(flight.getDepartureAirport().getCode() + ","
                                + flight.getDestinationAirport().getCode() + ","
                                + flight.getDepartureTime().format(TIME_FORMAT) + ","
                                + flight.getDurationMinutes()
                );
                writer.newLine();
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException("Fajl ne moze da se sacuva");
        }
    }

}