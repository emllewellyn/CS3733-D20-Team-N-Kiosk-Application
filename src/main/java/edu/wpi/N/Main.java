package edu.wpi.N;

import edu.wpi.N.database.*;
import edu.wpi.N.entities.memento.CareTaker;
import edu.wpi.N.entities.memento.Originator;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args)
      throws SQLException, DBException, ClassNotFoundException, IOException {
    MapDB.initDB();
    // ArduinoController periperal = new ArduinoController();
    // periperal.initialize();
    // MapDB.setKiosk("NSERV00301", 180);

    /*final String DEFAULT///_NODES = "csv/UPDATEDTeamNnodes.csv";
    final String DEFAULT_PATHS = "csv/UPDATEDTeamNedges.csv";
    final InputStream INPUT_NODES_DEFAULT = Main.class.getResourceAsStream(DEFAULT_NODES);
    final InputStream INPUT_EDGES_DEFAULT = Main.class.getResourceAsStream(DEFAULT_PATHS);
    CSVParser.parseCSV(INPUT_NODES_DEFAULT);
    CSVParser.parseCSV(INPUT_EDGES_DEFAULT);*/

    Originator originator = new Originator();
    CareTaker careTaker = new CareTaker();

    originator.setState("State #1");
    originator.setState("State #2");
    careTaker.add(originator.saveStateToMemento());

    originator.setState("State #3");
    careTaker.add(originator.saveStateToMemento());

    originator.setState("State #4");
    System.out.println("Current State: " + originator.getState());

    originator.getStateFromMemento(careTaker.get(0));
    System.out.println("First saved State: " + originator.getState());
    originator.getStateFromMemento(careTaker.get(1));
    System.out.println("Second saved State: " + originator.getState());

    App.launch(App.class, args);
  }
}
