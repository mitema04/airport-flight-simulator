package airports.view;

import airports.file.FileManager;
import airports.model.Airport;
import airports.model.AirportSystem;
import airports.model.Flight;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class DataPanel extends Panel {

    private static final Color PINK_BACKGROUND = new Color(255, 170, 205);
    private static final Color LIST_BACKGROUND = new Color(255, 225, 238);
    private static final Color EM_AIR = new Color(170, 20, 90);

    private final AirportSystem airportSystem;
    private final MainFrame owner;

    private final TextArea airportsList;
    private final TextArea flightsList;

    public DataPanel(MainFrame owner, AirportSystem airportSystem) {
        this.owner = owner;
        this.airportSystem = airportSystem;

        setLayout(new BorderLayout());
        setBackground(PINK_BACKGROUND);

        add(createSpacer(25, 1), BorderLayout.WEST);
        add(createSpacer(25, 1), BorderLayout.EAST);
        add(createSpacer(1, 15), BorderLayout.NORTH);
        add(createSpacer(1, 20), BorderLayout.SOUTH);

        Panel contentPanel = new Panel(new BorderLayout(15, 15));
        contentPanel.setBackground(PINK_BACKGROUND);
        add(contentPanel, BorderLayout.CENTER);

        Label titleLabel = new Label("EmAir", Label.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 42));
        titleLabel.setForeground(EM_AIR);
        titleLabel.setBackground(PINK_BACKGROUND);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        airportsList = new TextArea("", 15, 40, TextArea.SCROLLBARS_BOTH);
        flightsList = new TextArea("", 15, 30, TextArea.SCROLLBARS_BOTH);

        airportsList.setEditable(false);
        flightsList.setEditable(false);

        airportsList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        flightsList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        airportsList.setBackground(LIST_BACKGROUND);
        flightsList.setBackground(LIST_BACKGROUND);

        Panel listsPanel = new Panel(new GridLayout(1, 2, 20, 0));
        listsPanel.setBackground(PINK_BACKGROUND);

        Panel airportsPanel = new Panel(new BorderLayout());
        Panel flightsPanel = new Panel(new BorderLayout());

        airportsPanel.setBackground(PINK_BACKGROUND);
        flightsPanel.setBackground(PINK_BACKGROUND);

        airportsPanel.add(airportsList, BorderLayout.CENTER);
        flightsPanel.add(flightsList, BorderLayout.CENTER);

        listsPanel.add(airportsPanel);
        listsPanel.add(flightsPanel);

        contentPanel.add(listsPanel, BorderLayout.CENTER);

        Button addAirportButton = new Button("Dodaj aerodrom");
        Button addFlightButton = new Button("Dodaj let");
        Button loadCsvButton = new Button("Ucitaj CSV");
        Button loadJsonButton = new Button("Ucitaj JSON");
        Button saveCsvButton = new Button("Sacuvaj CSV");
        Button saveJsonButton = new Button("Sacuvaj JSON");

        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setBackground(PINK_BACKGROUND);

        buttonsPanel.add(addAirportButton);
        buttonsPanel.add(addFlightButton);
        buttonsPanel.add(loadCsvButton);
        buttonsPanel.add(loadJsonButton);
        buttonsPanel.add(saveCsvButton);
        buttonsPanel.add(saveJsonButton);

        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addAirportButton.addActionListener(event -> {
            AddAirportDialog dialog = new AddAirportDialog(owner, airportSystem);
            dialog.setVisible(true);
            refreshData();
        });

        addFlightButton.addActionListener(event -> {
            AddFlightDialog dialog = new AddFlightDialog(owner, airportSystem);
            dialog.setVisible(true);
            refreshData();
        });

        loadCsvButton.addActionListener(event -> loadCsv());
        loadJsonButton.addActionListener(event -> loadJson());
        saveCsvButton.addActionListener(event -> saveCsv());
        saveJsonButton.addActionListener(event -> saveJson());

        refreshData();
    }

    private Panel createSpacer(int width, int height) {
        Panel spacer = new Panel();
        spacer.setPreferredSize(new Dimension(width, height));
        spacer.setBackground(PINK_BACKGROUND);
        return spacer;
    }

    public void refreshData() {
        refreshAirports();
        refreshFlights();
    }

    private void refreshAirports() {
        StringBuilder text = new StringBuilder();

        text.append(String.format("%-30s %-6s %6s %6s%n", "Naziv", "Kod", "X", "Y"));
        text.append("---------------------------------------------------\n");

        for (Airport airport : airportSystem.getAirports())
            text.append(String.format(
                    "%-30.30s %-6s %6d %6d%n",
                    airport.getName(),
                    airport.getCode(),
                    airport.getX(),
                    airport.getY()
            ));

        airportsList.setText(text.toString());
        airportsList.setCaretPosition(0);
    }

    private void refreshFlights() {
        StringBuilder text = new StringBuilder();

        text.append(String.format("%-14s %-14s %-12s %-12s%n", "Od", "Do", "Vreme", "Trajanje"));
        text.append("---------------------------------------------------\n");

        for (Flight flight : airportSystem.getFlights())
            text.append(String.format(
                    "%-14s %-14s %-12s %d min%n",
                    flight.getDepartureAirport().getCode(),
                    flight.getDestinationAirport().getCode(),
                    flight.getDepartureTime(),
                    flight.getDurationMinutes()
            ));

        flightsList.setText(text.toString());
        flightsList.setCaretPosition(0);
    }

    private void loadCsv() {
        FileDialog fileDialog = new FileDialog(owner, "Izaberi CSV fajl", FileDialog.LOAD);
        fileDialog.setFilenameFilter((directory, name) -> name.toLowerCase().endsWith(".csv"));

        showFileDialog(fileDialog);

        if (fileDialog.getFile() == null)
            return;

        File file = new File(fileDialog.getDirectory(), fileDialog.getFile());

        try {
            FileManager.loadCsv(file, airportSystem);
            refreshData();
            showMessage("CSV fajl je uspesno ucitan", Color.BLACK);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), Color.RED);
        }
    }

    private void loadJson() {
        FileDialog fileDialog = new FileDialog(owner, "Izaberi JSON fajl", FileDialog.LOAD);
        fileDialog.setFilenameFilter((directory, name) -> name.toLowerCase().endsWith(".json"));

        showFileDialog(fileDialog);

        if (fileDialog.getFile() == null)
            return;

        File file = new File(fileDialog.getDirectory(), fileDialog.getFile());

        try {
            FileManager.loadJson(file, airportSystem);
            refreshData();
            showMessage("JSON fajl je uspesno ucitan", Color.BLACK);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), Color.RED);
        }
    }

    private void saveCsv() {
        FileDialog fileDialog = new FileDialog(owner, "Sacuvaj CSV fajl", FileDialog.SAVE);
        fileDialog.setFile("podaci.csv");

        showFileDialog(fileDialog);

        if (fileDialog.getFile() == null)
            return;

        String fileName = fileDialog.getFile();

        if (!fileName.toLowerCase().endsWith(".csv"))
            fileName += ".csv";

        File file = new File(fileDialog.getDirectory(), fileName);

        try {
            FileManager.saveCsv(file, airportSystem);
            showMessage("CSV fajl je uspesno sacuvan", Color.BLACK);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), Color.RED);
        }
    }

    private void saveJson() {
        FileDialog fileDialog = new FileDialog(owner, "Sacuvaj JSON fajl", FileDialog.SAVE);
        fileDialog.setFile("podaci.json");

        showFileDialog(fileDialog);

        if (fileDialog.getFile() == null)
            return;

        String fileName = fileDialog.getFile();

        if (!fileName.toLowerCase().endsWith(".json"))
            fileName += ".json";

        File file = new File(fileDialog.getDirectory(), fileName);

        try {
            FileManager.saveJson(file, airportSystem);
            showMessage("JSON fajl je uspesno sacuvan", Color.BLACK);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), Color.RED);
        }
    }

    private void showFileDialog(FileDialog fileDialog) {
        owner.setFileDialogOpen(true);

        try {
            fileDialog.setVisible(true);
        } finally {
            owner.setFileDialogOpen(false);
        }
    }

    private void showMessage(String message, Color textColor) {
        Dialog dialog = new Dialog(owner, "Poruka", true);
        dialog.setSize(450, 150);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setBackground(PINK_BACKGROUND);

        Label messageLabel = new Label(message, Label.CENTER);
        messageLabel.setForeground(textColor);
        messageLabel.setBackground(PINK_BACKGROUND);

        Button closeButton = new Button("U redu");
        closeButton.addActionListener(event -> dialog.dispose());

        Panel buttonsPanel = new Panel(new FlowLayout());
        buttonsPanel.setBackground(PINK_BACKGROUND);
        buttonsPanel.add(closeButton);

        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dialog.dispose();
            }
        });

        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}