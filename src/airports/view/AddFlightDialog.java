package airports.view;

import airports.model.Airport;
import airports.model.AirportSystem;
import airports.model.Flight;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddFlightDialog extends Dialog {

    private static final Color PINK_BACKGROUND = new Color(255, 170, 205);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AirportSystem airportSystem;

    private final Choice departureAirportChoice;
    private final Choice destinationAirportChoice;
    private final TextField departureTimeField;
    private final TextField durationField;

    private final Label messageLabel;

    public AddFlightDialog(Frame owner, AirportSystem airportSystem) {
        super(owner, "Dodavanje leta", true);

        this.airportSystem = airportSystem;

        setSize(350, 280);
        setLayout(new BorderLayout());
        setBackground(PINK_BACKGROUND);

        add(createSpacer(20, 1), BorderLayout.WEST);
        add(createSpacer(20, 1), BorderLayout.EAST);
        add(createSpacer(1, 15), BorderLayout.NORTH);
        add(createSpacer(1, 15), BorderLayout.SOUTH);

        Panel contentPanel = new Panel(new BorderLayout(10, 10));
        contentPanel.setBackground(PINK_BACKGROUND);
        add(contentPanel, BorderLayout.CENTER);

        departureAirportChoice = new Choice();
        destinationAirportChoice = new Choice();
        departureTimeField = new TextField(30);
        durationField = new TextField(30);

        for (Airport airport : airportSystem.getAirports()) {
            departureAirportChoice.add(airport.getCode());
            destinationAirportChoice.add(airport.getCode());
        }

        if (airportSystem.getAirports().size() > 1) {
            destinationAirportChoice.select(1);
        }

        Panel fieldsPanel = new Panel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setBackground(PINK_BACKGROUND);

        fieldsPanel.add(new Label("Pocetni aerodrom:"));
        fieldsPanel.add(departureAirportChoice);

        fieldsPanel.add(new Label("Krajnji aerodrom:"));
        fieldsPanel.add(destinationAirportChoice);

        fieldsPanel.add(new Label("Vreme poletanja:"));
        fieldsPanel.add(departureTimeField);

        fieldsPanel.add(new Label("Trajanje u minutima:"));
        fieldsPanel.add(durationField);

        contentPanel.add(fieldsPanel, BorderLayout.NORTH);

        messageLabel = new Label("", Label.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setBackground(PINK_BACKGROUND);

        Button addButton = new Button("Dodaj");
        Button cancelButton = new Button("Odustani");

        addButton.addActionListener(event -> addFlight());
        cancelButton.addActionListener(event -> dispose());

        Panel buttonsPanel = new Panel(
                new FlowLayout(FlowLayout.CENTER, 15, 5)
        );

        buttonsPanel.setBackground(PINK_BACKGROUND);
        buttonsPanel.add(addButton);
        buttonsPanel.add(cancelButton);

        Panel bottomPanel = new Panel(new BorderLayout(0, 5));
        bottomPanel.setBackground(PINK_BACKGROUND);
        bottomPanel.add(messageLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
        setLocationRelativeTo(owner);
    }

    private Panel createSpacer(int width, int height) {
        Panel spacer = new Panel();
        spacer.setPreferredSize(new Dimension(width, height));
        spacer.setBackground(PINK_BACKGROUND);
        return spacer;
    }

    private void addFlight() {
        try {
            if (airportSystem.getAirports().size() < 2) {
                throw new IllegalArgumentException("Moraju da postoje najmanje dva aerodroma.");
            }

            Airport departureAirport = airportSystem.findAirportByCode(departureAirportChoice.getSelectedItem());
            Airport destinationAirport = airportSystem.findAirportByCode(destinationAirportChoice.getSelectedItem());

            LocalTime departureTime = LocalTime.parse(departureTimeField.getText().trim(), TIME_FORMAT);
            int durationMinutes = Integer.parseInt(durationField.getText().trim());
            Flight flight = new Flight(
                    departureAirport,
                    destinationAirport,
                    departureTime,
                    durationMinutes
            );

            airportSystem.addFlight(flight);
            dispose();

        } catch (DateTimeParseException exception) {
            messageLabel.setText("Vreme mora biti u formatu HH:mm.");

        } catch (NumberFormatException exception) {
            messageLabel.setText("Trajanje mora biti ceo broj minuta.");

        } catch (IllegalArgumentException exception) {
            messageLabel.setText(exception.getMessage());
        }
    }
}