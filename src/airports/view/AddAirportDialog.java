package airports.view;

import airports.model.Airport;
import airports.model.AirportSystem;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddAirportDialog extends Dialog {

    private static final Color PINK_BACKGROUND = new Color(255, 170, 205);

    private final AirportSystem airportSystem;

    private final TextField nameField;
    private final TextField codeField;
    private final TextField xField;
    private final TextField yField;

    private final Label messageLabel;

    public AddAirportDialog(
            Frame owner,
            AirportSystem airportSystem
    ) {
        super(owner, "Dodavanje aerodroma", true);

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

        nameField = new TextField(30);
        codeField = new TextField(30);
        xField = new TextField(30);
        yField = new TextField(30);

        Panel fieldsPanel = new Panel(new GridLayout(4, 2, 10, 10));

        fieldsPanel.setBackground(PINK_BACKGROUND);

        fieldsPanel.add(new Label("Naziv aerodroma:"));
        fieldsPanel.add(nameField);

        fieldsPanel.add(new Label("Kod aerodroma:"));
        fieldsPanel.add(codeField);

        fieldsPanel.add(new Label("X koordinata:"));
        fieldsPanel.add(xField);

        fieldsPanel.add(new Label("Y koordinata:"));
        fieldsPanel.add(yField);

        contentPanel.add(fieldsPanel, BorderLayout.NORTH);

        messageLabel = new Label("", Label.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setBackground(PINK_BACKGROUND);


        Button addButton = new Button("Dodaj");
        Button cancelButton = new Button("Odustani");

        addButton.addActionListener(event -> addAirport());
        cancelButton.addActionListener(event -> dispose());

        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 5));

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

    private void addAirport() {
        try {
            int x = Integer.parseInt(xField.getText().trim());
            int y = Integer.parseInt(yField.getText().trim());
            Airport airport = new Airport(nameField.getText(), codeField.getText(), x, y);
            airportSystem.addAirport(airport);
            dispose();

        } catch (NumberFormatException exception) {
            messageLabel.setText("Koordinate moraju da budu celi brojevi.");

        } catch (IllegalArgumentException exception) {
            messageLabel.setText(exception.getMessage());
        }
    }
}