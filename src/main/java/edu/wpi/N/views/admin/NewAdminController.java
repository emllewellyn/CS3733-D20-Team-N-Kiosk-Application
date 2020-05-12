package edu.wpi.N.views.admin;

import com.jfoenix.controls.*;
import edu.wpi.N.App;
import edu.wpi.N.algorithms.AStar;
import edu.wpi.N.algorithms.BFS;
import edu.wpi.N.algorithms.DFS;
import edu.wpi.N.algorithms.Dijkstra;
import edu.wpi.N.database.*;
import edu.wpi.N.entities.Service;
import edu.wpi.N.entities.States.StateSingleton;
import edu.wpi.N.entities.employees.Employee;
import edu.wpi.N.entities.request.*;
import edu.wpi.N.views.Controller;
import edu.wpi.N.views.features.ArduinoController;
import edu.wpi.N.views.outdated.ViewRequestControllerOUTDATED;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NewAdminController implements Controller, Initializable {

  private App mainApp = null;
  DeleteEmployeeController deleteEmployeeController;
  private StateSingleton singleton;

  public NewAdminController(StateSingleton singleton) {
    this.singleton = singleton;
  }

  @FXML Label lbl_title;
  @FXML AnchorPane anchorSwap;
  @FXML TableView<Request> tb_RequestTable;
  @FXML JFXCheckBox ch_requestFilter;
  @FXML AnchorPane ap_swapPane;
  @FXML JFXButton btn_addDoc;
  @FXML JFXButton btn_arduino;
  @FXML JFXButton btn_denyReq;
  @FXML JFXButton btn_acceptReq;
  @FXML JFXButton btn_upload;
  @FXML JFXButton btn_editMap;
  @FXML JFXButton btn_editEmp;
  @FXML JFXButton btn_addEmp;
  @FXML JFXButton btn_remEmp;
  @FXML TableView<Employee> tbl_Employees;
  @FXML ChoiceBox<Service> cb_reqFilter;
  @FXML JFXButton btn_admin;
  @FXML JFXComboBox cb_changeAlgo;
  @FXML JFXButton btn_submit;
  @FXML Label lbl_algo;
  @FXML Label lbl_changeAlgo;

  private ObservableList<Request> tableData = FXCollections.observableArrayList();
  private ObservableList<Employee> emps = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      setTitleLabel();
      populateRequestTable();
      initTooltips();
      tableSetup();
      populateTable();
      populateByType();
      populateEmployeeType();
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      lbl_algo.setVisible(false);
      lbl_changeAlgo.setVisible(false);
      populateChangeAlgo();
      lbl_algo.setText(singleton.algoState);
    } catch (DBException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
    initializeTable();
  }

  @FXML
  private void initTooltips() {
    btn_acceptReq.setTooltip(new Tooltip("Accepts a Given Request"));
    btn_addDoc.setTooltip(new Tooltip("Adds a Doctor"));
    btn_addEmp.setTooltip(new Tooltip("Adds an Employee"));
    btn_arduino.setTooltip(new Tooltip("Opens Arduino Menu"));
    btn_denyReq.setTooltip(new Tooltip("Denies a given Request"));
    btn_editEmp.setTooltip(new Tooltip("Edits Employees"));
    btn_editMap.setTooltip(new Tooltip("Opens Map Editor"));
    btn_remEmp.setTooltip(new Tooltip("Removes a Given Employee"));
    btn_upload.setTooltip(new Tooltip("File Manager"));
    btn_admin.setTooltip(new Tooltip("Adds an Admin"));
  }

  @FXML
  private void hideAlgo() {
    btn_submit.setVisible(false);
    cb_changeAlgo.setVisible(false);
    lbl_algo.setVisible(false);
    lbl_changeAlgo.setVisible(false);
  }

  @FXML
  private void changeAlgo() {
    btn_submit.setVisible(true);
    cb_changeAlgo.setVisible(true);
    lbl_algo.setVisible(true);
    lbl_changeAlgo.setVisible(true);
    ap_swapPane.setVisible(false);
  }

  @FXML
  private void addEmp() {
    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("addEmployee.fxml"));
      AnchorPane currentpane = loader.load();
      AddEmployeeController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void addAdmin() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("adminPage.fxml"));
      AnchorPane currentpane = loader.load();
      AddAdminController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void delEmp() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("deleteEmployee.fxml"));
      AnchorPane currentpane = loader.load();
      DeleteEmployeeController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void addDoc() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("addDoctor.fxml"));
      AnchorPane currentpane = loader.load();
      AddDoctorController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void denyReq() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("denyRequest.fxml"));
      AnchorPane currentpane = loader.load();
      DenyRequestController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void editEmp() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("editEmployee.fxml"));
      AnchorPane currentpane = loader.load();
      EditEmployeeController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void assignReq() {
    try {
      ap_swapPane.setVisible(true);
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("acceptRequest.fxml"));
      AnchorPane currentpane = loader.load();
      RequestHandelerController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
      hideAlgo();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Pops up a new window with File Manager */
  @FXML
  private void popUpFileManager() {
    try {
      System.out.println("here");
      Stage stage = new Stage();
      Parent root;

      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("fileManagementScreen.fxml"));

      loader.setControllerFactory(
          type -> {
            try {
              // Inject singleton into DataEditorController
              return new DataEditorController(singleton);
            } catch (Exception exc) {
              exc.printStackTrace();
              throw new RuntimeException(exc);
            }
          });

      root = loader.load();

      Controller controller = loader.getController();
      controller.setMainApp(mainApp);

      Scene scene = new Scene(root);
      stage.setScene(scene);

      stage.initModality(Modality.APPLICATION_MODAL);
      stage.show();
    } catch (Exception ex) {
      ex.printStackTrace();
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Oops... Something went Wong");
      errorAlert.setContentText("Error when openning File Manager Window");
      errorAlert.showAndWait();
    }
  }

  @FXML
  public void logoutUser() throws DBException {
    try {
      LoginDB.logout();
      mainApp.switchScene("views/admin/newLogin.fxml", singleton);
    } catch (DBException | IOException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
  }

  @Override
  public void setMainApp(App mainApp) {
    this.mainApp = mainApp;
  }

  public void editMap() throws IOException {
    System.out.println("here");
    mainApp.switchScene("views/mapEditor/mapEditor.fxml", singleton);
  }

  public void setTitleLabel() throws DBException {
    try {
      lbl_title.setText("Welcome, " + LoginDB.currentLogin());
    } catch (DBException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
  }

  @FXML
  public void loadArduino() {

    try {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(getClass().getResource("arduinoInterface.fxml"));
      AnchorPane currentpane = loader.load();
      ArduinoController controller = loader.getController();
      controller.setAdminController(this);
      ap_swapPane.getChildren().setAll(currentpane);
      ap_swapPane.setVisible(true);
      btn_submit.setVisible(false);
      cb_changeAlgo.setVisible(false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void populateRequestTable() throws DBException {
    LinkedList<Request> reqs = ServiceDB.getRequests();
    tableData.setAll(reqs);

    ch_requestFilter
        .selectedProperty()
        .addListener(
            (ov, old, val) -> {
              try {
                if (val) {
                  LinkedList<Request> rqs = ServiceDB.getOpenRequests();
                  tableData.setAll(rqs);
                } else {
                  LinkedList<Request> rqs = ServiceDB.getRequests();
                  tableData.setAll(rqs);
                }
              } catch (DBException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setContentText(e.getMessage());
                errorAlert.show();
              }
            });

    tb_RequestTable.setItems(tableData);
  }

  private void initializeTable() {

    // Request Table
    TableColumn<Request, Integer> requestID = new TableColumn<>("ID");
    requestID.setMaxWidth(50);
    requestID.setMinWidth(50);
    requestID.setCellValueFactory(new PropertyValueFactory<Request, Integer>("requestID"));

    TableColumn<Request, Employee> emp_assigned = new TableColumn<>("Assigned");
    emp_assigned.setMaxWidth(100);
    emp_assigned.setMinWidth(100);
    emp_assigned.setCellValueFactory(new PropertyValueFactory<Request, Employee>("emp_assigned"));

    TableColumn<Request, String> notes = new TableColumn<>("Notes");
    notes.setMaxWidth(150);
    notes.setMinWidth(150);
    notes.setCellValueFactory(new PropertyValueFactory<Request, String>("reqNotes"));

    TableColumn<Request, String> nodeID = new TableColumn<>("Location");
    nodeID.setMaxWidth(100);
    nodeID.setMinWidth(100);
    nodeID.setCellValueFactory(new ViewRequestControllerOUTDATED.nodeLongName());

    TableColumn<Request, String> status = new TableColumn<>("Status");
    status.setMaxWidth(100);
    status.setMinWidth(100);
    status.setCellValueFactory(new PropertyValueFactory<Request, String>("status"));

    TableColumn<Request, String> attr1 = new TableColumn<>("Attribute 1");
    attr1.setMaxWidth(100);
    attr1.setMinWidth(100);
    attr1.setCellValueFactory(new PropertyValueFactory<Request, String>("Atr1"));

    TableColumn<Request, String> service = new TableColumn<>("Service");
    service.setMaxWidth(75);
    service.setMinWidth(75);
    service.setCellValueFactory(new PropertyValueFactory<Request, String>("serviceType"));

    TableColumn<Request, String> attr2 = new TableColumn<>("Attribute 2");
    attr2.setMaxWidth(100);
    attr2.setMinWidth(100);
    attr2.setCellValueFactory(new PropertyValueFactory<Request, String>("Atr2"));

    TableColumn<Request, String> attr3 = new TableColumn<>("Attribute 3");
    attr3.setMaxWidth(100);
    attr3.setMinWidth(100);
    attr3.setCellValueFactory(new PropertyValueFactory<Request, String>("Atr3"));

    TableColumn<Request, String> attr4 = new TableColumn<>("Attribute 4");
    attr4.setMaxWidth(100);
    attr4.setMinWidth(100);
    attr4.setCellValueFactory(new PropertyValueFactory<Request, String>("Atr4"));

    tb_RequestTable
        .getColumns()
        .addAll(
            requestID, service, emp_assigned, notes, nodeID, status, attr1, attr2, attr3, attr4);
  }

  public void populateTable() throws DBException {
    try {
      tbl_Employees.getItems().clear();
      LinkedList<Employee> empList = ServiceDB.getEmployees();
      emps.addAll(empList);
      tbl_Employees.setItems(emps);

    } catch (DBException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
  }

  public void tableSetup() {
    TableColumn<Employee, Integer> empID = new TableColumn<>("ID");
    empID.setMaxWidth(100);
    empID.setMinWidth(100);
    empID.setCellValueFactory(new PropertyValueFactory<Employee, Integer>("ID"));

    TableColumn<Employee, String> name = new TableColumn<>("Name");
    name.setMaxWidth(150);
    name.setMinWidth(150);
    name.setCellValueFactory(new PropertyValueFactory<Employee, String>("name"));

    TableColumn<Employee, Service> serviceType = new TableColumn<>("Service Type");
    serviceType.setMaxWidth(150);
    serviceType.setMinWidth(150);
    serviceType.setCellValueFactory(new PropertyValueFactory<Employee, Service>("ServiceType"));

    tbl_Employees.getColumns().addAll(empID, name, serviceType);
  }

  public void deleteEmployee() throws DBException {
    try {

      if (tbl_Employees.getSelectionModel().getSelectedIndex() <= -1) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText("Select an Employee");
        errorAlert.show();

        return;
      }

      int employeeIndex = tbl_Employees.getSelectionModel().getSelectedIndex();
      ServiceDB.removeEmployee(tbl_Employees.getSelectionModel().getSelectedItem().getID());

      Alert confAlert = new Alert(Alert.AlertType.CONFIRMATION);
      confAlert.setContentText("Employee removed");
      confAlert.show();

      int removeLine = tbl_Employees.getSelectionModel().getSelectedIndex();
      tbl_Employees.getItems().remove(removeLine);
    } catch (DBException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
  }

  @FXML
  public void acceptRow() {
    try {

      if (tb_RequestTable.getSelectionModel().getSelectedItem() == null) {

        Alert invalidID = new Alert(Alert.AlertType.ERROR);
        invalidID.setContentText("No request selected");
        invalidID.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DENY")) {

        Alert invalidID = new Alert(Alert.AlertType.ERROR);
        invalidID.setContentText("Service was already declined.");
        invalidID.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DONE")) {

        Alert invalidID = new Alert(Alert.AlertType.ERROR);
        invalidID.setContentText("Service was already completed.");
        invalidID.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getEmp_assigned() == null) {

        Alert invalidID = new Alert(Alert.AlertType.ERROR);
        invalidID.setContentText("Assign an Employee first.");
        invalidID.show();

        return;
      }

      ServiceDB.completeRequest(
          tb_RequestTable.getSelectionModel().getSelectedItems().get(0).getRequestID(), "");

      Alert acceptReq = new Alert(Alert.AlertType.CONFIRMATION);
      acceptReq.setContentText("Request Accepted");
      acceptReq.show();

      if (ch_requestFilter.isSelected()) {
        tb_RequestTable.getItems().removeAll(tb_RequestTable.getSelectionModel().getSelectedItem());
      } else {
        LinkedList<Request> reqs = ServiceDB.getRequests();
        tableData.setAll(reqs);
      }

    } catch (DBException ex) {
      ex.printStackTrace();
    }
  }

  public void assignEmployeeToRequest(Employee employee) throws DBException {
    Alert confAlert = new Alert(Alert.AlertType.CONFIRMATION);
    try {

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DENY")) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText("Request was already denied.");
        errorAlert.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DONE")) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText("Request has been completed.");
        errorAlert.show();

        return;
      }

      ServiceDB.assignToRequest(
          employee.getID(), tb_RequestTable.getSelectionModel().getSelectedItem().getRequestID());
      confAlert.setContentText(employee.getName() + " was assigned to the request");
      confAlert.show();
    } catch (DBException e) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText(e.getMessage());
      errorAlert.show();
    }
    populateRequestTable();
  }

  @FXML
  public void denyRow(String compNotes) {
    try {

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DENY")) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText("Request was already denied.");
        errorAlert.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("DONE")) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText("Request was already completed.");
        errorAlert.show();

        return;
      }

      if (tb_RequestTable.getSelectionModel().getSelectedItem().getStatus().equals("OPEN")) {
        ServiceDB.denyRequest(
            tb_RequestTable.getSelectionModel().getSelectedItems().get(0).getRequestID(),
            compNotes);

        Alert denyReq = new Alert(Alert.AlertType.WARNING);
        denyReq.setContentText("Request Denied");
        denyReq.show();

        return;
      }

      if (ch_requestFilter.isSelected()) {
        tb_RequestTable.getItems().removeAll(tb_RequestTable.getSelectionModel().getSelectedItem());
      } else {
        LinkedList<Request> reqs = ServiceDB.getRequests();
        tableData.setAll(reqs);
      }

    } catch (DBException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void assignPressed(Employee employee) throws DBException {
    int rID;
    try {
      rID = tb_RequestTable.getSelectionModel().getSelectedItem().getRequestID();
    } catch (NullPointerException indx) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setContentText("Please select a request and an employee!");
      errorAlert.show();
      return;
    }
    assignEmployeeToRequest(employee);
  }

  public void populateByType() {
    cb_reqFilter
        .valueProperty()
        .addListener(
            (ob, old, newVal) -> {
              if (newVal.getServiceType().equals("Laundry")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Laundry")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Translator")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Translator")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Wheelchair")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Wheelchair")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("IT")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("IT")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Security")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Security")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Flower")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Flower")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Internal Transport")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Internal Transport")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Emotional Support")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Emotional Support")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else if (newVal.getServiceType().equals("Sanitation")) {
                ObservableList<Request> reqs = FXCollections.observableArrayList();

                try {
                  for (Request req : ServiceDB.getRequests()) {
                    if (req.getServiceType().equals("Sanitation")) {
                      reqs.add(req);
                    }
                  }

                  tb_RequestTable.setItems(reqs);
                } catch (DBException e) {
                  e.printStackTrace();
                }
              } else {
                try {
                  ObservableList<Request> req = FXCollections.observableArrayList();
                  req.setAll(ServiceDB.getRequests());
                } catch (DBException e) {
                  e.printStackTrace();
                }
              }
            });
  }

  public void populateEmployeeType() throws DBException {
    LinkedList<Service> employeeList = new LinkedList<Service>();
    ObservableList<Service> empTypeList = FXCollections.observableArrayList();

    for (Service service : ServiceDB.getServices()) {
      if (!service.getServiceType().equals("Medicine")) {
        employeeList.add(service);
      }
    }
    empTypeList.setAll(employeeList);
    cb_reqFilter.setItems(empTypeList);
  }

  public void populateChangeAlgo() {
    LinkedList<String> algoTypes = new LinkedList<>();
    algoTypes.add("BFS");
    algoTypes.add("DFS");
    algoTypes.add("AStar");
    algoTypes.add("Dijkstra");
    ObservableList<String> algos = FXCollections.observableArrayList();
    algos.addAll(algoTypes);
    cb_changeAlgo.setItems(algos);
  }

  @FXML
  public void changeAlgorithm() {
    if (cb_changeAlgo.getSelectionModel().getSelectedItem().equals("BFS")) {
      singleton.savedAlgo.setPathFinder(new BFS());
      lbl_algo.setText("BFS");
      singleton.algoState = "BFS";

    } else if (cb_changeAlgo.getSelectionModel().getSelectedItem().equals("DFS")) {
      singleton.savedAlgo.setPathFinder(new DFS());
      lbl_algo.setText("DFS");
      singleton.algoState = "DFS";

    } else if (cb_changeAlgo.getSelectionModel().getSelectedItem().equals("AStar")) {
      singleton.savedAlgo.setPathFinder(new AStar());
      lbl_algo.setText("AStar");
      singleton.algoState = "AStar";

    } else if (cb_changeAlgo.getSelectionModel().getSelectedItem().equals("Dijkstra")) {
      singleton.savedAlgo.setPathFinder(new Dijkstra());
      lbl_algo.setText("Dijkstra");
      singleton.algoState = "Dijkstra";
    }
  }

  public void resetTable() {
    tb_RequestTable.getItems().clear();
    try {
      populateRequestTable();
    } catch (DBException e) {
      e.printStackTrace();
    }
  }

  public String getAlgoInstance() {
    return lbl_algo.getText();
  }
}
