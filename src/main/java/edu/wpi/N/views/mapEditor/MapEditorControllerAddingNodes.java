package edu.wpi.N.views.mapEditor;

import com.google.common.collect.HashBiMap;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import edu.wpi.N.AppClass;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.MapDB;
import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.States.StateSingleton;
import edu.wpi.N.entities.UIEdge;
import edu.wpi.N.entities.UINode;
import edu.wpi.N.views.Controller;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public class MapEditorControllerAddingNodes implements Controller {

  AppClass mainApp;

  private StateSingleton singleton;

  @FXML Pane pn_display;
  @FXML Pane pn_editor;
  @FXML Pane pn_elev;
  @FXML Button btn_home;
  @FXML StackPane pn_stack;
  @FXML Pane pn_edges;
  @FXML Pane pn_floors;
  @FXML ImageView img_map;
  @FXML JFXButton btn_cancel_elev;
  @FXML Label lbl_building_floor;

  final int DEFAULT_FLOOR = 1;
  final String DEFAULT_BUILDING = "Faulkner";
  final Color DEFAULT_CIRCLE_COLOR = Color.DARKORCHID;
  final Color DEFAULT_LINE_COLOR = Color.BLACK;
  double DEFAULT_LINE_WIDTH = 4;
  final Color ADD_NODE_COLOR = Color.BLACK;
  final Color DELETE_NODE_COLOR = Color.RED;
  final Color EDIT_NODE_COLOR = Color.RED;
  double DEFAULT_CIRCLE_OPACITY = .75;
  double DEFAULT_CIRCLE_RADIUS = 7;
  final Color DELETE_EDGE_COLOR = Color.RED;
  final Color EDIT_ELEV_COLOR = Color.RED;
  final Color EDIT_ELEV_SELECTED_COLOR = Color.GREEN;

  final double SCREEN_WIDTH = 1920;
  final double SCREEN_HEIGHT = 1080;
  double IMAGE_WIDTH = 2475;
  double IMAGE_HEIGHT = 1485;
  double MAP_WIDTH = 1661;
  double MAP_HEIGHT = 997;
  double HORIZONTAL_SCALE = MAP_WIDTH / IMAGE_WIDTH;
  double VERTICAL_SCALE = MAP_HEIGHT / IMAGE_HEIGHT;
  int numFloors;

  // Zoom constants
  private final double MIN_MAP_SCALE = 0.8;
  private final double MAX_MAP_SCALE = 4;
  private final double ZOOM_STEP_SCROLL = 0.01;
  private final double ZOOM_STEP_BUTTON = 0.1;
  private DoubleProperty mapScaleAlpha = new SimpleDoubleProperty(0);
  private double clickStartX, clickStartY;

  private Timeline autoFocus = new Timeline();
  private LinkedList<KeyValue> endFocusVals = new LinkedList<>();

  private boolean isDraggingNode = false;

  public String hitboxNodeId;

  Mode mode;

  private enum Mode {
    NO_STATE,
    ADD_NODE,
    DELETE_NODE,
    EDIT_NODE,
    ADD_EDGE,
    DELETE_EDGE,
    EDIT_ELEV,
    ALIGN_NODE,
    ADD_SHAFT
  }

  HashBiMap<Circle, UINode> nodesMap;
  HashBiMap<Line, UIEdge> edgesMap;
  MapEditorAddNodeController controllerAddNode;
  MapEditorDeleteNodeController controllerDeleteNode;
  MapEditorEditNodeController controllerEditNode;
  MapEditorDeleteEdgeController controllerDeleteEdge;
  BetweenFloorsController controllerEditElev;
  MapEditorAlignNodeController controllerAlignNode;
  MapEditorAddShaftController controllerAddShaft;

  int currentFloor;
  String currentBuilding;
  // Add Node Variable
  Circle addNodeCircle;
  // Delete Node Variable
  LinkedList<Circle> deleteNodeCircles;
  // Edit Node Variable
  Circle editNodeCircle;
  // Add Edge Variable
  Line addEdgeLine;
  // Delete Edge Variable
  LinkedList<Line> deleteEdgeLines;
  // Edit Edit Elevator Variable
  LinkedList<Circle> editElevNodes;
  Circle elevCircle;
  LinkedList<Circle> alignNodeCircles;
  LinkedList<DbNode> addShaftNodeCircles;
  HashMap<DbNode, Circle> nodesMap2;
  LinkedList<DbNode> originalShaftNodes;

  // Floor switching
  JFXNodesList buildingButtonList;
  JFXNodesList faulknerButtonList;
  JFXNodesList mainButtonList;

  // Inject singleton
  public MapEditorControllerAddingNodes(StateSingleton singleton) {
    this.singleton = singleton;
  }

  @Override
  public void setMainApp(AppClass mainApp) {
    this.mainApp = mainApp;
  }

  public void initialize() throws DBException, IOException {
    setFaulknerDefaults();
    nodesMap2 = new HashMap<>();
    currentFloor = DEFAULT_FLOOR;
    currentBuilding = DEFAULT_BUILDING;
    // initializeChangeFloorButtons();
    // initializeMainCampusFloorButtons();
    // setFloorButtonColors();
    editElevNodes = new LinkedList<>();
    // btn_cancel_elev.setDisable(true);
    btn_cancel_elev.setVisible(false);
    nodesMap = HashBiMap.create();
    edgesMap = HashBiMap.create();
    mode = Mode.NO_STATE;
    loadFloor();
    addNodeCircle = null;
    elevCircle = null;
    deleteNodeCircles = new LinkedList<>();
    alignNodeCircles = new LinkedList<>();
    addShaftNodeCircles = new LinkedList<>();

    editNodeCircle = null;
    addEdgeLine = new Line();
    addEdgeLine.setStrokeLineCap(StrokeLineCap.ROUND);
    pn_edges.getChildren().add(addEdgeLine);
    deleteEdgeLines = new LinkedList<>();

    setFloorBuildingText(this.currentFloor, this.currentBuilding);
    this.buildingButtonList = new JFXNodesList();
    this.faulknerButtonList = new JFXNodesList();
    this.mainButtonList = new JFXNodesList();

    initFloorButtons();
    initAutoFocus();
  }

  private void setFaulknerDefaults() {
    DEFAULT_CIRCLE_OPACITY = .75;
    DEFAULT_LINE_WIDTH = 4;
    DEFAULT_CIRCLE_RADIUS = 7;
    IMAGE_WIDTH = 2475;
    IMAGE_HEIGHT = 1485;
    MAP_WIDTH = 1661;
    MAP_HEIGHT = 997;
    HORIZONTAL_SCALE = MAP_WIDTH / IMAGE_WIDTH;
    VERTICAL_SCALE = MAP_HEIGHT / IMAGE_HEIGHT;
    numFloors = 5;
  }

  private void setMainCampusDefaults() {
    DEFAULT_CIRCLE_OPACITY = .85;
    DEFAULT_LINE_WIDTH = 2;
    DEFAULT_CIRCLE_RADIUS = 3;
    IMAGE_WIDTH = 5000;
    IMAGE_HEIGHT = 3400;
    MAP_WIDTH = 1465;
    MAP_HEIGHT = 994;
    HORIZONTAL_SCALE = MAP_WIDTH / IMAGE_WIDTH;
    VERTICAL_SCALE = MAP_HEIGHT / IMAGE_HEIGHT;
    numFloors = 6;
  }

  private void loadFloor() throws DBException, IOException {
    clearNodes();
    clearEdges();
    nodesMap = HashBiMap.create();
    edgesMap = HashBiMap.create();
    LinkedList<DbNode> floorNodes;
    LinkedList<DbNode[]> floorEdges;
    if (currentBuilding.equals("Faulkner")) {
      floorNodes = MapDB.floorNodes(currentFloor, currentBuilding);
      floorEdges = MapDB.getFloorEdges(currentFloor, currentBuilding);
    } else {
      floorNodes = MapDB.NobuildingfloorNodes(currentFloor);
      // floorEdges = new LinkedList<>();
      floorEdges = MapDB.nobuildingFloorEdges(currentFloor);
    }
    try {
      HashMap<String, UINode> conversion = createUINodes(floorNodes, DEFAULT_CIRCLE_COLOR);
      createUIEdges(conversion, floorEdges, DEFAULT_LINE_COLOR);
    } catch (IOException e) {
      e.printStackTrace();
    }
    displayEdges();
    displayNodes();
    if (mode.equals(Mode.ADD_SHAFT)) {
      try {
        reloadAddShaft();
      } catch (DBException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void displayEdges() {
    for (Line line : edgesMap.keySet()) {
      pn_edges.getChildren().add(line);
    }
  }

  private void displayNodes() {
    for (Circle circle : nodesMap.keySet()) {
      circle.setOpacity(DEFAULT_CIRCLE_OPACITY);
      pn_display.getChildren().add(circle);
    }
  }

  private void clearEdges() {
    for (Line line : edgesMap.keySet()) {
      pn_edges.getChildren().removeIf(node -> node instanceof Line);
    }
  }

  private void clearNodes() {
    for (Circle circle : nodesMap.keySet()) {
      pn_display.getChildren().removeIf(node -> node instanceof Circle);
    }
  }

  private HashMap<String, UINode> createUINodes(LinkedList<DbNode> nodes, Color c)
      throws IOException {
    HashMap<String, UINode> conversion = new HashMap<>();
    for (DbNode DBnode : nodes) {
      Circle circle = createCircle(scaleX(DBnode.getX()), scaleY(DBnode.getY()), c);
      UINode UInode = new UINode(circle, DBnode);
      nodesMap.put(circle, UInode);
      nodesMap2.put(DBnode, circle);
      if (DBnode.getNodeType().equals("STAI") || DBnode.getNodeType().equals("ELEV")) {
        editElevNodes.add(circle);
      }
      conversion.put(DBnode.getNodeID(), UInode);
    }
    return conversion;
  }

  private void createUIEdges(
      HashMap<String, UINode> conversion, LinkedList<DbNode[]> edges, Color c) {
    for (DbNode[] edge : edges) {
      Line line =
          createLine(
              scaleX(edge[0].getX()),
              scaleY(edge[0].getY()),
              scaleX(edge[1].getX()),
              scaleY(edge[1].getY()),
              c);
      line.setStrokeLineCap(StrokeLineCap.ROUND);
      line.setOnMouseClicked(event -> this.handleLineClickedEvents(event, line));
      line.setCursor(Cursor.HAND);
      UIEdge UIedge = new UIEdge(line, edge);
      conversion.get(edge[0].getNodeID()).addEdge(UIedge);
      conversion.get(edge[1].getNodeID()).addEdge(UIedge);
      edgesMap.put(line, UIedge);
    }
  }

  private Circle createCircle(double x, double y, Color c) throws IOException {
    Circle circle = new Circle();
    circle.setRadius(DEFAULT_CIRCLE_RADIUS);
    circle.setCenterX(x);
    circle.setCenterY(y);
    circle.setFill(c);
    circle.setOpacity(DEFAULT_CIRCLE_OPACITY);
    circle.setCursor(Cursor.HAND);
    circle.setOnMouseDragged(event -> this.handleCircleDragEvents(event, circle));
    circle.setOnMouseClicked(
        event -> {
          try {
            this.handleCircleClickedEvents(event, circle);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
    circle.setOnMouseReleased(
        event -> {
          try {
            this.handleCircleDragReleased(event, circle);
          } catch (DBException e) {
            e.printStackTrace();
          }
        });
    //    pn_display.getChildren().add(circle);
    return circle;
  }

  private Line createLine(double x1, double y1, double x2, double y2, Color c) {
    Line line = new Line(x1, y1, x2, y2);
    line.setStroke(c);
    line.setStrokeLineCap(StrokeLineCap.ROUND);
    line.setStrokeWidth(DEFAULT_LINE_WIDTH);
    pn_display.getChildren().add(line);
    return line;
  }

  private double scaleX(double x) {
    return x * HORIZONTAL_SCALE;
  }

  private double scaleY(double y) {
    return y * VERTICAL_SCALE;
  }

  private void changeEditor() throws IOException {
    resetAllExceptShafts();
    if (mode == Mode.ADD_NODE) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorAddNode.fxml");
    } else if (mode == Mode.DELETE_NODE) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorDeleteNode.fxml");
    } else if (mode == Mode.EDIT_NODE) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorEditNode.fxml");
    } else if (mode == Mode.DELETE_EDGE) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorDeleteEdge.fxml");
    } else if (mode == Mode.ALIGN_NODE) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorAlignNode.fxml");
    } else if (mode == Mode.ADD_SHAFT) {
      loadEditor("/edu/wpi/N/views/mapEditor/mapEditorAddShaft.fxml");
    } else if (mode == Mode.EDIT_ELEV) {
      loadEditor("/edu/wpi/N/views/mapEditor/BetweenFloorsEditor.fxml");
    }
  }

  private void loadEditor(String path) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
    Pane pane = loader.load();
    if (mode == Mode.ADD_NODE) {
      controllerAddNode = loader.getController();
    } else if (mode == Mode.DELETE_NODE) {
      controllerDeleteNode = loader.getController();
    } else if (mode == Mode.EDIT_NODE) {
      controllerEditNode = loader.getController();
    } else if (mode == Mode.ALIGN_NODE) {
      controllerAlignNode = loader.getController();
    } else if (mode == Mode.DELETE_EDGE) {
      controllerDeleteEdge = loader.getController();
    } else if (mode == Mode.ADD_SHAFT) {
      controllerAddShaft = loader.getController();
    } else if (mode == Mode.EDIT_ELEV) {
      controllerEditElev = loader.getController();
      controllerEditElev.setMainApp(mainApp);
    }
    if (mode == Mode.EDIT_ELEV) {
      pn_elev.getChildren().add(pane);
      pn_elev.setVisible(true);
      controllerEditElev.setFloor(currentFloor, currentBuilding);
      onBtnAddShaftClicked();
      pn_editor.setVisible(false);

    } else {
      pn_editor.getChildren().add(pane);
      pn_editor.setVisible(true);
    }
  }

  @FXML
  private void onIconClicked(MouseEvent event) {
    // Nothing yet
  }

  private void handleCircleDragEvents(MouseEvent event, Circle circle) {
    isDraggingNode = true;
    circle.setCursor(Cursor.CLOSED_HAND);
    if (mode == Mode.ADD_NODE && circle == addNodeCircle) {
      onCircleAddNodeDragged(event, circle);
    }
    if (mode == Mode.EDIT_NODE) {
      //      onBtnCancelEditNodeClicked();
      //      onBtnConfirmEditNodeClicked();
      onTxtPosEditNodeTextChanged(circle);
      onCircleEditNodeDragged(event, circle);
    }
    if (mode == Mode.ADD_EDGE || mode == Mode.NO_STATE) {
      mode = Mode.ADD_EDGE;
      handleCircleAddEdgeDragged(event, circle);
    }
  }

  private void handleLineClickedEvents(MouseEvent event, Line line) {
    if (mode == Mode.DELETE_EDGE) {
      onLineDeleteEdgeClicked(event, line);
    }
  }

  private void onLineDeleteEdgeClicked(MouseEvent event, Line line) {
    if (line.getStroke() == DEFAULT_LINE_COLOR) {
      line.setStroke(DELETE_EDGE_COLOR);
      DbNode[] edge = edgesMap.get(line).getDBNodes();
      deleteEdgeLines.add(line);
      controllerDeleteEdge.addLstDeleteNode(edge[0].getShortName() + ", " + edge[1].getShortName());
    } else if (line.getStroke() == DELETE_EDGE_COLOR) {
      line.setStroke(DEFAULT_LINE_COLOR);
      DbNode[] edge = edgesMap.get(line).getDBNodes();
      controllerDeleteEdge.removeLstDeleteNode(
          edge[0].getShortName() + ", " + edge[1].getShortName());
      deleteEdgeLines.remove(line);
    }
  }

  private void cancelDeleteEdge() {
    controllerDeleteEdge
        .getBtnCancel()
        .setOnMouseClicked(
            e -> {
              resetDeleteEdge();
              mode = Mode.NO_STATE;
            });
  }

  private void confirmDeleteEdge() {
    controllerDeleteEdge
        .getBtnConfirm()
        .setOnMouseClicked(
            e -> {
              for (Line line : deleteEdgeLines) {
                DbNode[] nodes = edgesMap.get(line).getDBNodes();
                pn_edges.getChildren().remove(line);
                edgesMap.remove(line);
                for (UINode node : nodesMap.values()) {
                  if (node.getDBNode().getNodeID() == nodes[0].getNodeID()
                      || node.getDBNode().getNodeID() == nodes[1].getNodeID()) {
                    node.getEdges().remove(nodesMap.get(line));
                  }
                }
                try {
                  MapDB.removeEdge(nodes[0].getNodeID(), nodes[1].getNodeID());
                } catch (DBException ex) {
                  ex.printStackTrace();
                }
              }
              resetDeleteEdge();
              mode = Mode.NO_STATE;
            });
  }

  private void resetDeleteEdge() {
    pn_editor.setVisible(false);
    pn_stack.getChildren().remove(pn_display);
    pn_stack.getChildren().add(pn_display);
    /*
    if (!pn_display.getChildren().contains(pn_editor)) {
      pn_display.getChildren().add(pn_editor);
    }

     */
    for (Line line : deleteEdgeLines) {
      line.setStroke(DEFAULT_LINE_COLOR);
    }
    deleteEdgeLines.clear();
  }

  private void handleCircleClickedEvents(MouseEvent event, Circle circle)
      throws DBException, IOException {
    if (mode == Mode.DELETE_NODE) {
      onCircleDeleteNodeClicked(event, circle);
    }
    if (mode == Mode.ALIGN_NODE) {
      onCircleAlignNodeClicked(event, circle);
    }
    if (mode == Mode.EDIT_NODE) {
      onBtnCancelEditNodeClicked();
      onBtnConfirmEditNodeClicked();
      onTxtPosEditNodeTextChanged(circle);
      onCircleEditNodeClicked(event, circle);
    }
    if (mode == Mode.ADD_SHAFT) {
      onCircleAddShaftNodeClicked(event, circle);
    }
    if (mode == Mode.EDIT_ELEV && editElevNodes.contains(circle)) {

      //      onBtnSaveEditElevClicked();

      onBtnCancelEditElevClicked();
      onBtnAddShaftClicked();
      if (elevCircle != null && elevCircle != circle) {
        elevCircle.setFill(EDIT_ELEV_COLOR);
        pn_elev.setVisible(false);
      }
      elevCircle = circle;
      elevCircle.setFill(EDIT_ELEV_SELECTED_COLOR);
      controllerEditElev.setFloor(
          nodesMap.get(circle).getDBNode().getFloor(),
          nodesMap.get(circle).getDBNode().getBuilding());
      controllerEditElev.setNode(nodesMap.get(circle).getDBNode());
      pn_elev.setVisible(true);
    }
  }

  private void onCircleEditNodeClicked(MouseEvent event, Circle circle) {
    if (editNodeCircle != circle && editNodeCircle != null) {
      DbNode node = nodesMap.get(editNodeCircle).getDBNode();
      editNodeCircle.setCenterX(scaleX(node.getX()));
      editNodeCircle.setCenterY(scaleY(node.getY()));
      editNodeCircle.setFill(DEFAULT_CIRCLE_COLOR);
      cancelEditNode();
    }
    editNodeCircle = circle;
    circle.setFill(EDIT_NODE_COLOR);
    DbNode node = nodesMap.get(circle).getDBNode();
    controllerEditNode.setShortName(node.getShortName());
    controllerEditNode.setLongName(node.getLongName());
    controllerEditNode.setPos(circle.getCenterX(), circle.getCenterY());

    // autoFocusToNode(node);
  }

  private void onCircleEditNodeDragged(MouseEvent event, Circle circle) {
    if (editNodeCircle != null && editNodeCircle != circle) {
      DbNode node = nodesMap.get(editNodeCircle).getDBNode();
      editNodeCircle.setCenterX(scaleX(node.getX()));
      editNodeCircle.setCenterY(scaleY(node.getY()));
      editNodeCircle.setFill(DEFAULT_CIRCLE_COLOR);
      cancelEditNode();
    }
    editNodeCircle = circle;
    circle.setFill(EDIT_NODE_COLOR);
    controllerEditNode.setPos(event.getX(), event.getY());
    circle.setCenterX(event.getX());
    circle.setCenterY(event.getY());
    DbNode node = nodesMap.get(circle).getDBNode();
    controllerEditNode.setShortName(node.getShortName());
    controllerEditNode.setLongName(node.getLongName());
    UINode uiNode = nodesMap.get(circle);
    for (UIEdge edges : uiNode.getEdges()) {
      DbNode firstNode = edges.getDBNodes()[0];
      DbNode secondNode = edges.getDBNodes()[1];
      if (firstNode.getNodeID().equals(node.getNodeID())) {
        edges.getLine().setStartX(event.getX());
        edges.getLine().setStartY(event.getY());
      } else if (secondNode.getNodeID().equals(node.getNodeID())) {
        edges.getLine().setEndX(event.getX());
        edges.getLine().setEndY(event.getY());
      }
    }
  }

  private void cancelEditNode() {
    DbNode node = nodesMap.get(editNodeCircle).getDBNode();
    UINode uiNode = nodesMap.get(editNodeCircle);
    for (UIEdge edges : uiNode.getEdges()) {
      DbNode firstNode = edges.getDBNodes()[0];
      DbNode secondNode = edges.getDBNodes()[1];
      if (firstNode.getNodeID().equals(node.getNodeID())) {
        edges.getLine().setStartX(scaleX(uiNode.getDBNode().getX()));
        edges.getLine().setStartY(scaleY(uiNode.getDBNode().getY()));
      } else if (secondNode.getNodeID().equals(node.getNodeID())) {
        edges.getLine().setEndX(scaleX(uiNode.getDBNode().getX()));
        edges.getLine().setEndY(scaleY(uiNode.getDBNode().getY()));
      }
    }
  }

  private void onCircleDeleteNodeClicked(MouseEvent event, Circle circle) {

    String deleteNodeType = nodesMap.get(circle).getDBNode().getNodeType();
    hitboxNodeId = nodesMap.get(circle).getDBNode().getNodeID();

    if (circle.getFill() == DEFAULT_CIRCLE_COLOR) {
      circle.setFill(DELETE_NODE_COLOR);
      deleteNodeCircles.add(circle);
      controllerDeleteNode.addLstDeleteNode(nodesMap.get(circle).getDBNode().getNodeID());
    } else if (circle.getFill() == DELETE_NODE_COLOR) {
      circle.setFill(DEFAULT_CIRCLE_COLOR);
      deleteNodeCircles.remove(circle);
      controllerDeleteNode.removeLstDeleteNode(nodesMap.get(circle).getDBNode().getNodeID());
    }

    autoFocusToNodesGroup(deleteNodeCircles);
  }

  private void onCircleAlignNodeClicked(MouseEvent event, Circle circle) {
    String alignNodeID = nodesMap.get(circle).getDBNode().getNodeID();
    if (circle.getFill() == DEFAULT_CIRCLE_COLOR) {
      circle.setFill(DELETE_NODE_COLOR);
      alignNodeCircles.add(circle);
      controllerAlignNode.addLstAlignNode(nodesMap.get(circle).getDBNode().getShortName());
    } else if (circle.getFill() == DELETE_NODE_COLOR) {
      circle.setFill(DEFAULT_CIRCLE_COLOR);
      alignNodeCircles.remove(circle);
      controllerAlignNode.removeLstAlignNode(nodesMap.get(circle).getDBNode().getShortName());
    }
    autoFocusToNodesGroup(alignNodeCircles);
  }

  private void onCircleAddShaftNodeClicked(MouseEvent event, Circle circle) {
    if (circle.getFill() == Color.CADETBLUE) {
      circle.setFill(Color.BLACK);
      addShaftNodeCircles.add(nodesMap.get(circle).getDBNode());
      controllerAddShaft.addLstAddShaftNode(nodesMap.get(circle).getDBNode().getLongName());

    } else if (circle.getFill() == Color.BLACK) {
      circle.setFill(Color.CADETBLUE);
      addShaftNodeCircles.remove(nodesMap.get(circle).getDBNode());
      controllerAddShaft.removeLstAddShaftNode(nodesMap.get(circle).getDBNode().getLongName());
    }
  }

  private void handleDeleteNodeRightClick() throws IOException, DBException {
    mode = Mode.DELETE_NODE;
    hideEditElevButton();
    changeEditor();
    onBtnCancelDeleteNodeClicked();
    onBtnConfirmDeleteNodeClicked();
  }

  private void handleAlignNodeRightClick() throws IOException, DBException {
    mode = Mode.ALIGN_NODE;
    hideEditElevButton();
    changeEditor();
    onBtnCancelAlignNodeClicked();
    onBtnConfirmAlignNodeClicked();
  }

  private void handleEditNodeRightClick() throws IOException {
    mode = Mode.EDIT_NODE;
    hideEditElevButton();
    changeEditor();
    onBtnCancelEditNodeClicked();
    onBtnConfirmEditNodeClicked();
  }

  private void handleDeleteEdgeRightClick() throws IOException {
    mode = Mode.DELETE_EDGE;
    hideEditElevButton();
    changeEditor();
    pn_stack.getChildren().remove(pn_edges);
    pn_stack.getChildren().add(pn_edges);
    // pn_edges.getChildren().add(pn_editor);
    cancelDeleteEdge();
    confirmDeleteEdge();
  }

  // Pane Display Clicked
  public void onPaneDisplayClicked(MouseEvent event) throws IOException {
    // Add Node
    System.out.println("yo Clicked");
    System.out.println(event.getScreenX());
    System.out.println(event.getScreenY());
    if (event.getClickCount() == 2 && mode != Mode.ADD_NODE) {
      hideEditElevButton();
      onPaneDisplayClickedAddNode(event);
    }
    if (event.getButton() == MouseButton.SECONDARY) {
      ContextMenu menu = new ContextMenu();
      MenuItem deleteNode = new MenuItem("Delete Node");
      MenuItem editNode = new MenuItem("Edit Node");
      MenuItem alignNode = new MenuItem("Align Nodes");
      MenuItem deleteEdge = new MenuItem("Delete Edge");
      MenuItem editElev = new MenuItem("Edit Elevator/Stair Edges");

      deleteNode.setOnAction(
          e -> {
            try {
              //  resetAll();
              handleDeleteNodeRightClick();
            } catch (IOException | DBException ex) {
              ex.printStackTrace();
            }
          });
      editNode.setOnAction(
          e -> {
            try {
              //   resetAll();
              handleEditNodeRightClick();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          });
      deleteEdge.setOnAction(
          e -> {
            try {
              // resetAll();
              handleDeleteEdgeRightClick();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          });
      alignNode.setOnAction(
          e -> {
            try {
              // resetAll();
              handleAlignNodeRightClick();
            } catch (IOException | DBException ex) {
              ex.printStackTrace();
            }
          });
      editElev.setOnAction(
          e -> {
            try {
              // resetAll();
              handleEditElevRightClick();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          });
      menu.getItems().addAll(deleteNode, editNode, deleteEdge, editElev, alignNode);
      menu.show(mainApp.getStage(), event.getScreenX(), event.getScreenY());
    }
  }

  private void handleEditElevRightClick() throws IOException {
    mode = Mode.EDIT_ELEV;
    btn_cancel_elev.setDisable(false);
    btn_cancel_elev.setVisible(true);
    changeEditor();
    for (Circle circle : editElevNodes) {
      circle.setFill(EDIT_ELEV_COLOR);
    }
  }

  private void onPaneDisplayClickedAddNode(MouseEvent event) throws IOException {
    System.out.println("click is workin");
    double x = event.getX();
    double y = event.getY();
    int actualX = (int) scaleXDB(x);
    int actualY = (int) scaleYDB(y);
    controllerDeleteNode.addXandY(actualX, actualY);
  }

  public void onCircleAddNodeDragged(MouseEvent event, Circle circle) {
    circle.setCenterX(event.getX());
    circle.setCenterY(event.getY());
    controllerAddNode.setPos(event.getX(), event.getY());
  }

  // Add Node FXML
  public void onTxtPosAddNodeTextChanged(Circle circle) {
    controllerAddNode
        .getTxtXPos()
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> setCircleXPosAddNode(circle, newValue)));
    controllerAddNode
        .getTxtYPos()
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> setCircleYPosAddNode(circle, newValue)));
  }

  public void onTxtPosEditNodeTextChanged(Circle circle) {
    controllerEditNode
        .getTxtXPos()
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> setCircleXPosEditNode(circle, newValue)));
    controllerEditNode
        .getTxtYPos()
        .textProperty()
        .addListener(((observable, oldValue, newValue) -> setCircleYPosEditNode(circle, newValue)));
  }

  private void setCircleXPosAddNode(Circle circle, String newValue) {
    try {
      circle.setCenterX(Double.parseDouble(newValue));

    } catch (NumberFormatException e) {
      controllerAddNode.setPos(circle.getCenterX(), circle.getCenterY());
      displayErrorMessage("Invalid Input");
    }
  }

  private void setCircleYPosAddNode(Circle circle, String newValue) {
    try {
      circle.setCenterY(Double.parseDouble(newValue));
    } catch (NumberFormatException e) {
      controllerAddNode.setPos(circle.getCenterX(), circle.getCenterY());
      displayErrorMessage("Invalid Input");
    }
  }

  private void setCircleXPosEditNode(Circle circle, String newValue) {
    try {
      if (editNodeCircle == circle || (mode != Mode.EDIT_NODE)) {
        circle.setCenterX(Double.parseDouble(newValue));
        DbNode node = nodesMap.get(circle).getDBNode();
        UINode uiNode = nodesMap.get(circle);
        for (UIEdge edges : uiNode.getEdges()) {
          DbNode firstNode = edges.getDBNodes()[0];
          DbNode secondNode = edges.getDBNodes()[1];
          if (firstNode.getNodeID().equals(Double.parseDouble(newValue))) {
            edges.getLine().setStartX(Double.parseDouble(newValue));
          } else if (secondNode.getNodeID().equals(node.getNodeID())) {
            edges.getLine().setEndX(Double.parseDouble(newValue));
          }
        }
      }
    } catch (NumberFormatException e) {
      controllerEditNode.setPos(circle.getCenterX(), circle.getCenterY());
      displayErrorMessage("Invalid Input");
    }
  }

  private void setCircleYPosEditNode(Circle circle, String newValue) {
    try {
      if (editNodeCircle == circle || (mode != Mode.EDIT_NODE)) {
        circle.setCenterY(Double.parseDouble(newValue));
        DbNode node = nodesMap.get(circle).getDBNode();
        UINode uiNode = nodesMap.get(circle);
        for (UIEdge edges : uiNode.getEdges()) {
          DbNode firstNode = edges.getDBNodes()[0];
          DbNode secondNode = edges.getDBNodes()[1];
          if (firstNode.getNodeID().equals(Double.parseDouble(newValue))) {
            edges.getLine().setStartY(Double.parseDouble(newValue));
          } else if (secondNode.getNodeID().equals(node.getNodeID())) {
            edges.getLine().setEndY(Double.parseDouble(newValue));
          }
        }
      }
    } catch (NumberFormatException e) {
      controllerEditNode.setPos(circle.getCenterX(), circle.getCenterY());
      displayErrorMessage("Invalid Input");
    }
  }

  private void setCircleXPosAlignNode(Circle circle, double newValue) {
    try {
      if (editNodeCircle == circle || (mode != Mode.EDIT_NODE)) {
        circle.setCenterX(newValue);
        DbNode node = nodesMap.get(circle).getDBNode();
        UINode uiNode = nodesMap.get(circle);
        for (UIEdge edges : uiNode.getEdges()) {
          DbNode firstNode = edges.getDBNodes()[0];
          DbNode secondNode = edges.getDBNodes()[1];
          if (firstNode.getNodeID().equals(newValue)) {
            edges.getLine().setStartX(newValue);
          } else if (secondNode.getNodeID().equals(node.getNodeID())) {
            edges.getLine().setEndX(newValue);
          }
        }
      }
    } catch (NumberFormatException e) {
      displayErrorMessage("Invalid Input");
    }
  }

  private void setCircleYPosAlignNode(Circle circle, double newValue) {
    try {
      if (editNodeCircle == circle || (mode != Mode.EDIT_NODE)) {
        circle.setCenterY(newValue);
        DbNode node = nodesMap.get(circle).getDBNode();
        UINode uiNode = nodesMap.get(circle);
        for (UIEdge edges : uiNode.getEdges()) {
          DbNode firstNode = edges.getDBNodes()[0];
          DbNode secondNode = edges.getDBNodes()[1];
          if (firstNode.getNodeID().equals(newValue)) {
            edges.getLine().setStartY(newValue);
          } else if (secondNode.getNodeID().equals(node.getNodeID())) {
            edges.getLine().setEndY(newValue);
          }
        }
      }
    } catch (NumberFormatException e) {
      displayErrorMessage("Invalid Input");
    }
  }

  private void onBtnConfirmAddNodeClicked(Circle circle) {
    controllerAddNode
        .getBtnConfirm()
        .setOnMouseClicked(
            event -> {
              try {
                mode = Mode.NO_STATE;
                String strX = controllerAddNode.getXPos();
                String strY = controllerAddNode.getYPos();
                int x = 0;
                int y = 0;
                try {
                  x = (int) scaleXDB(Double.parseDouble(strX));
                  y = (int) scaleYDB(Double.parseDouble(strY));
                } catch (NumberFormatException e) {
                  displayErrorMessage("Invalid input");
                  return;
                }
                String type = controllerAddNode.getType();
                String longName = controllerAddNode.getLongName();
                String shortName = controllerAddNode.getShortName();
                String building = controllerAddNode.getBuilding();
                if (type == null || longName == null || shortName == null || building == null) {
                  displayErrorMessage("Invalid input");
                  return;
                }
                DbNode newNode =
                    MapDB.addNode(x, y, currentFloor, building, type, longName, shortName);
                //                LinkedList list = new LinkedList();
                //                list.add(newNode);
                //                createUINodes(list, DEFAULT_CIRCLE_COLOR);
                Circle circle1 = createCircle(scaleX(x), scaleY(y), DEFAULT_CIRCLE_COLOR);
                UINode uiNode = new UINode(circle1, newNode);
                nodesMap.put(circle1, uiNode);
                pn_display.getChildren().add(circle1);
                pn_display.getChildren().remove(circle);
                //                pn_display.getChildren().add();
                pn_editor.setVisible(false);
              } catch (DBException | IOException e) {
                e.printStackTrace();
              }
            });
  }

  private void onBtnConfirmDeleteNodeClicked() throws DBException {
    controllerDeleteNode
        .getBtnConfirm()
        .setOnMouseClicked(
            event -> {
              LinkedList<Integer> xandYpos = controllerDeleteNode.getxandY();
              int x1 = xandYpos.get(0);
              int y1 = xandYpos.get(1);
              int x2 = xandYpos.get(2);
              int y2 = xandYpos.get(3);
              /*try {
                MapDB.addHitbox(x1,y1,x2,y2,hitboxNodeId);
              } catch (DBException e) {
                e.printStackTrace();
              }*/
              controllerDeleteNode.lst_delete_node.getItems().clear();
              controllerDeleteNode.clearXandY();
              resetDeleteNode();
              mode = Mode.NO_STATE;
            });
  }

  private void onBtnConfirmEditNodeClicked() {
    controllerEditNode
        .getBtnConfirm()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              String strX = controllerEditNode.getXPos();
              String strY = controllerEditNode.getYPos();
              int x = 0;
              int y = 0;
              try {
                x = (int) scaleXDB(Double.parseDouble(strX));
                y = (int) scaleYDB(Double.parseDouble(strY));
              } catch (NumberFormatException e) {
                displayErrorMessage("Invalid input");
                return;
              }
              String shortName = controllerEditNode.getShortName();
              String longName = controllerEditNode.getLongName();
              if (longName == null || shortName == null) {
                displayErrorMessage("Invalid input");
                return;
              }
              String id = nodesMap.get(editNodeCircle).getDBNode().getNodeID();
              try {
                MapDB.modifyNode(id, x, y, longName, shortName);
                DbNode newNode = MapDB.getNode(id);
                nodesMap.remove(editNodeCircle);
                UINode UInode = new UINode(editNodeCircle, newNode);
                nodesMap.put(editNodeCircle, UInode);
              } catch (DBException e) {
                e.printStackTrace();
              }
              pn_editor.setVisible(false);
              editNodeCircle.setFill(DEFAULT_CIRCLE_COLOR);
              editNodeCircle = null;
            });
  }

  private void onBtnAddShaftClicked() throws IOException {
    controllerEditElev
        .getBtnAddShaft()
        .setOnMouseClicked(
            event -> {
              mode = Mode.ADD_SHAFT;
              try {
                reloadAddShaft();
                if (!controllerEditElev.getNodesInShaft().isEmpty()) {
                  for (DbNode c : controllerEditElev.getNodesInShaft()) {
                    if (!addShaftNodeCircles.contains(c))
                      controllerAddShaft.addLstAddShaftNode(c.getLongName());
                    addShaftNodeCircles.add(c);
                    if (c.getFloor() == currentFloor) {
                      nodesMap2.get(c).setFill(Color.BLACK);
                    }
                  }
                }
                originalShaftNodes = addShaftNodeCircles;
              } catch (DBException e) {
                e.printStackTrace();
              }
            });
  }

  private void onBtnConfirmAddShaftClicked() throws DBException {
    controllerAddShaft
        .getBtnConfirm()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              // Check valid input
              if (addShaftNodeCircles.isEmpty()) {
                displayErrorMessage("Must select nodes");
                cancelAddShaft();
                return;
              }
              String nodeType = addShaftNodeCircles.getFirst().getNodeType();
              String building = addShaftNodeCircles.getFirst().getBuilding();
              for (DbNode n : addShaftNodeCircles) {
                if (!n.getNodeType().equals(nodeType)) {
                  displayErrorMessage("Nodes must be the same type");
                  cancelAddShaft();
                  return;
                } else if (!n.getBuilding().equals(building)) {
                  displayErrorMessage("Nodes must be in the same building");
                  cancelAddShaft();
                  return;
                }
              }
              for (int i = 0; i < addShaftNodeCircles.size() - 1; i++) {
                for (int j = i + 1; j < addShaftNodeCircles.size(); j++) {
                  if (addShaftNodeCircles.get(i).getFloor()
                      == addShaftNodeCircles.get(j).getFloor()) {
                    displayErrorMessage("Nodes must be on different floors");
                    cancelAddShaft();
                    return;
                  }
                }
              }
              // remove nodes from shaft
              for (DbNode n : addShaftNodeCircles) {
                try {
                  if (isInShaft(n)) {
                    MapDB.removeFromShaft(n.getNodeID());
                  }
                } catch (DBException e) {
                  e.printStackTrace();
                }
              }
              for (DbNode n : originalShaftNodes) {
                try {
                  if (isInShaft(n)) {
                    MapDB.removeFromShaft(n.getNodeID());
                  }
                } catch (DBException e) {
                  e.printStackTrace();
                }
              }
              // remove edges with other nodes in shafts
              for (DbNode n : addShaftNodeCircles) {
                try {
                  for (DbNode a : MapDB.getAdjacent(n.getNodeID())) {
                    if (a.getNodeType().equals("ELEV") || a.getNodeType().equals("STAI")) {
                      MapDB.removeEdge(n.getNodeID(), a.getNodeID());
                    }
                  }
                } catch (DBException e) {
                  e.printStackTrace();
                }
              }

              for (DbNode n : originalShaftNodes) {
                try {
                  for (DbNode a : MapDB.getAdjacent(n.getNodeID())) {
                    if (a.getNodeType().equals("ELEV") || a.getNodeType().equals("STAI")) {
                      MapDB.removeEdge(n.getNodeID(), a.getNodeID());
                    }
                  }
                } catch (DBException e) {
                  e.printStackTrace();
                }
              }
              // sort nodes list by floor
              LinkedList<DbNode> sortedNodes = new LinkedList<>();
              for (int i = 1; i <= numFloors; i++) {
                for (DbNode n : addShaftNodeCircles) {
                  if (n.getFloor() == i) {
                    sortedNodes.add(n);
                  }
                }
              }

              // add edges between each sorted node
              if (sortedNodes.size() >= 1) {
                for (int i = 0; i < sortedNodes.size() - 1; i++) {
                  try {
                    MapDB.addEdge(
                        sortedNodes.get(i).getNodeID(), sortedNodes.get(i + 1).getNodeID());
                  } catch (DBException e) {
                    e.printStackTrace();
                  }
                }
              }

              resetAddShaft();
              pn_editor.setVisible(false);

              pn_elev.setDisable(false);
              controllerEditElev.setFloor(currentFloor, currentBuilding);
              for (Circle c : editElevNodes) {
                c.setFill(DEFAULT_CIRCLE_COLOR);
              }
            });
  }

  private void onBtnConfirmAlignNodeClicked() {
    controllerAlignNode
        .getBtnConfirm()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              if (alignNodeCircles.isEmpty()) {
                displayErrorMessage("Must select nodes");
                return;
              }
              double centerX = alignNodeCircles.getFirst().getCenterX();
              double centerY = alignNodeCircles.getFirst().getCenterY();
              mode = Mode.NO_STATE;
              int x = 0;
              int y = 0;
              if (!(controllerAlignNode.getPos() == null)) {
                String str = controllerAlignNode.getPos();

                try {
                  if (controllerAlignNode.getAlignXSelected()) {
                    x = (int) scaleXDB(Double.parseDouble(str));
                    // centerX =

                  } else if (controllerAlignNode.getAlignYSelected()) {
                    y = (int) scaleYDB(Double.parseDouble(str));
                    // centerY =

                  } else {
                    displayErrorMessage("Invalid input");
                  }
                } catch (NumberFormatException e) {
                  displayErrorMessage("Invalid input");
                  return;
                }
              } else {
                x = nodesMap.get(alignNodeCircles.getFirst()).getDBNode().getX();
                y = nodesMap.get(alignNodeCircles.getFirst()).getDBNode().getY();
                centerX = unscaleXDB(x);
                centerY = unscaleYDB(y);
              }
              try {
                if (controllerAlignNode.getAlignXSelected()) {
                  for (Circle c : alignNodeCircles) {
                    String id = nodesMap.get(c).getDBNode().getNodeID();
                    MapDB.modifyNode(
                        id,
                        x,
                        MapDB.getNode(id).getY(),
                        MapDB.getNode(id).getLongName(),
                        MapDB.getNode(id).getShortName());
                    DbNode newNode = MapDB.getNode(id);
                    nodesMap.remove(c);
                    UINode UInode = new UINode(c, newNode);
                    nodesMap.put(c, UInode);
                    setCircleXPosAlignNode(c, centerX);
                  }
                } else if (controllerAlignNode.getAlignYSelected()) {
                  for (Circle c : alignNodeCircles) {
                    String id = nodesMap.get(c).getDBNode().getNodeID();
                    MapDB.modifyNode(
                        id,
                        MapDB.getNode(id).getX(),
                        y,
                        MapDB.getNode(id).getLongName(),
                        MapDB.getNode(id).getShortName());
                    DbNode newNode = MapDB.getNode(id);
                    nodesMap.remove(c);
                    UINode UInode = new UINode(c, newNode);
                    nodesMap.put(c, UInode);
                    setCircleYPosAlignNode(c, centerY);
                  }
                }

              } catch (DBException e) {
                e.printStackTrace();
              }
              controllerAlignNode.clearAllFields();
              pn_editor.setVisible(false);
              resetAlignNode();
            });
  }

  private void onBtnCancelAddNodeClicked() {
    controllerAddNode
        .getBtnCancel()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              pn_editor.setVisible(false);
              pn_display.getChildren().remove(addNodeCircle);
            });
  }

  private void onBtnCancelDeleteNodeClicked() {
    controllerDeleteNode
        .getBtnCancel()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              for (Circle circle : deleteNodeCircles) {
                circle.setFill(DEFAULT_CIRCLE_COLOR);
              }
              deleteNodeCircles.clear();
              pn_editor.setVisible(false);
            });
  }

  private void onBtnCancelAddShaftClicked() {
    controllerAddShaft
        .getBtnCancel()
        .setOnMouseClicked(
            event -> {
              controllerAddShaft.clearAllFields();
              pn_editor.setVisible(false);
              pn_elev.setDisable(false);
              resetAddShaft();
              for (Circle c : editElevNodes) {
                c.setFill(DEFAULT_CIRCLE_COLOR);
              }
            });
  }

  private void cancelAddShaft() {
    controllerAddShaft.clearAllFields();
    pn_editor.setVisible(false);
    pn_elev.setDisable(false);
    resetAddShaft();
    for (Circle c : editElevNodes) {
      c.setFill(DEFAULT_CIRCLE_COLOR);
    }
  }

  private void onBtnCancelAlignNodeClicked() {
    controllerAlignNode
        .getBtnCancel()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              for (Circle circle : alignNodeCircles) {
                circle.setFill(DEFAULT_CIRCLE_COLOR);
              }
              alignNodeCircles.clear();
              pn_editor.setVisible(false);
            });
  }

  private void onBtnCancelEditNodeClicked() {
    controllerEditNode
        .getBtnCancel()
        .setOnMouseClicked(
            event -> {
              mode = Mode.NO_STATE;
              pn_editor.setVisible(false);
              if (editNodeCircle != null) {
                editNodeCircle.setFill(DEFAULT_CIRCLE_COLOR);
                editNodeCircle.setCenterX(scaleX(nodesMap.get(editNodeCircle).getDBNode().getX()));
                editNodeCircle.setCenterY(scaleY(nodesMap.get(editNodeCircle).getDBNode().getY()));
                cancelEditNode();
              }
              editNodeCircle = null;
            });
  }

  private void onBtnCancelEditElevClicked() {
    controllerEditElev
        .getBtnCancel()
        .setOnMouseClicked(
            e -> {
              elevCircle.setFill(EDIT_ELEV_COLOR);
              elevCircle = null;
              pn_elev.setVisible(true);
            });
  }

  private void onBtnSaveEditElevClicked() {
    controllerEditElev
        .getBtnSave()
        .setOnMouseClicked(
            e -> {
              elevCircle.setFill(EDIT_ELEV_COLOR);
              elevCircle = null;
              pn_elev.setVisible(false);
            });
  }

  private void resetAllExceptShafts() {
    // pn_editor.setVisible(false);
    resetAddNode();
    resetDeleteNode();
    resetEditNode();
    resetDeleteEdge();
    resetAlignNode();
    resetEditElev();
  }

  private void resetAll() {
    pn_editor.setVisible(false);
    pn_elev.setVisible(false);
    resetAddNode();
    resetDeleteNode();
    resetEditNode();
    resetDeleteEdge();
    resetEditElev();
    resetAlignNode();
    resetAddShaft();
  }

  private void resetEditElev() {
    //    btn_cancel_elev.setDisable(true);
    //    btn_cancel_elev.setVisible(false);

    for (Circle circle : editElevNodes) {
      circle.setFill(DEFAULT_CIRCLE_COLOR);
    }
    editNodeCircle = null;
    pn_elev.setVisible(false);
    pn_elev.getChildren().clear();
  }

  private void resetAddNode() {
    if (addNodeCircle != null) {
      pn_display.getChildren().remove(addNodeCircle);
    }
  }

  private void resetDeleteNode() {
    for (Circle circle : deleteNodeCircles) {
      circle.setFill(DEFAULT_CIRCLE_COLOR);
    }
    deleteNodeCircles.clear();
    MapEditorDeleteNodeController.xandY.clear();
  }

  private void resetAddShaft() {
    addShaftNodeCircles.clear();
  }

  private void resetAlignNode() {
    for (Circle circle : alignNodeCircles) {
      circle.setFill(DEFAULT_CIRCLE_COLOR);
    }
    alignNodeCircles.clear();
  }

  private void resetEditNode() {
    if (editNodeCircle != null) {
      editNodeCircle.setFill(DEFAULT_CIRCLE_COLOR);

      editNodeCircle.setCenterX(scaleX(nodesMap.get(editNodeCircle).getDBNode().getX()));

      editNodeCircle.setCenterY(scaleY(nodesMap.get(editNodeCircle).getDBNode().getY()));
      cancelEditNode();
    }
    editNodeCircle = null;
  }

  private double scaleXDB(double x) {
    return x / HORIZONTAL_SCALE;
  }

  private double scaleYDB(double y) {
    return y / VERTICAL_SCALE;
  }

  private double unscaleXDB(double x) {
    return x * HORIZONTAL_SCALE;
  }

  private double unscaleYDB(double y) {
    return y * VERTICAL_SCALE;
  }

  public void displayErrorMessage(String str) {
    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    errorAlert.setHeaderText("Invalid input");
    errorAlert.setContentText(str);
    errorAlert.showAndWait();
  }

  public void onBtnHomeClicked() throws IOException {
    mainApp.switchScene("views/admin/adminPortal.fxml", singleton);
  }

  private void handleCircleAddEdgeDragged(MouseEvent event, Circle circle) {
    setLinePosition(
        addEdgeLine, circle.getCenterX(), circle.getCenterY(), event.getX(), event.getY());
  }

  private void setLinePosition(Line line, double x1, double y1, double x2, double y2) {
    line.setStartX(x1);
    line.setStartY(y1);
    line.setEndX(x2);
    line.setEndY(y2);
    line.setStrokeWidth(DEFAULT_LINE_WIDTH);
  }

  private void handleCircleDragReleased(MouseEvent event, Circle circle) throws DBException {
    LinkedList<DbNode> nodes = new LinkedList();
    LinkedList<UINode> UInode = new LinkedList();
    LinkedList<Circle> circles = new LinkedList();
    isDraggingNode = false;
    circle.setCursor(Cursor.HAND);
    if (mode == mode.ADD_EDGE) {
      for (Circle aCircle : nodesMap.keySet()) {
        if (aCircle.contains(addEdgeLine.getStartX(), addEdgeLine.getStartY())
            || aCircle.contains(addEdgeLine.getEndX(), addEdgeLine.getEndY())) {
          nodes.add(nodesMap.get(aCircle).getDBNode());
          UInode.add(nodesMap.get(aCircle));
          circles.add(aCircle);
        }
      }
      if (nodes.size() == 2) {
        MapDB.addEdge(nodes.get(0).getNodeID(), nodes.get(1).getNodeID());
        DbNode[] nodes1 = {nodes.get(0), nodes.get(1)};
        DbNode[] nodes2 = {nodes.get(1), nodes.get(0)};
        UIEdge edge;
        Line line =
            new Line(
                addEdgeLine.getStartX(),
                addEdgeLine.getStartY(),
                addEdgeLine.getEndX(),
                addEdgeLine.getEndY());
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeWidth(DEFAULT_LINE_WIDTH);
        line.setOnMouseClicked(e -> this.handleLineClickedEvents(event, line));

        if (circles.get(0).contains(addEdgeLine.getStartX(), addEdgeLine.getStartY())) {
          edge = new UIEdge(line, nodes1);
        } else {
          edge = new UIEdge(line, nodes2);
        }
        edgesMap.put(line, edge);
        addEdgeLine.setOnMouseClicked(e -> this.handleLineClickedEvents(event, edge.getLine()));
        pn_edges.getChildren().add(edge.getLine());
        pn_edges.getChildren().remove(addEdgeLine);
        UInode.get(0).addEdge(edge);
        UInode.get(1).addEdge(edge);
      }
      nodes.clear();
      pn_edges.getChildren().remove(addEdgeLine);
      addEdgeLine = new Line();
      addEdgeLine.setStrokeLineCap(StrokeLineCap.ROUND);
      pn_edges.getChildren().add(addEdgeLine);
      mode = Mode.NO_STATE;
    }
  }

  /*
  public void initializeChangeFloorButtons() {
    btn_floors = new JFXButton("Floors");
    btn_floor1 = new JFXButton("1");
    btn_floor2 = new JFXButton("2");
    btn_floor3 = new JFXButton("3");
    btn_floor4 = new JFXButton("4");
    btn_floor5 = new JFXButton("5");
    btn_floor6 = new JFXButton("Switch");
    btn_floors.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor1.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor2.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor3.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor4.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor5.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor6.setButtonType(JFXButton.ButtonType.RAISED);
    nodesListF = new JFXNodesList();
    nodesListF.addAnimatedNode(btn_floors);
    nodesListF.addAnimatedNode(btn_floor5);
    nodesListF.addAnimatedNode(btn_floor4);
    nodesListF.addAnimatedNode(btn_floor3);
    nodesListF.addAnimatedNode(btn_floor2);
    nodesListF.addAnimatedNode(btn_floor1);
    nodesListF.addAnimatedNode(btn_floor6);

    nodesListF.setSpacing(10);
    pn_changeFloor.getChildren().add(nodesListF);
    btn_floors
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floors.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor1
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor1.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor1.setOnMouseClicked(
        e -> {
          currentFloor = 1;
          setFloorImg("/edu/wpi/N/images/map/Floor1Reclor.png");
          setFloorButtonColors();
        });
    btn_floor2
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor2.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor2.setOnMouseClicked(
        e -> {
          currentFloor = 2;
          setFloorImg("/edu/wpi/N/images/map/Floor2TeamN.png");
          setFloorButtonColors();
        });
    btn_floor3
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor3.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor3.setOnMouseClicked(
        e -> {
          currentFloor = 3;
          setFloorImg("/edu/wpi/N/images/map/Floor3TeamN.png");
          setFloorButtonColors();
        });
    btn_floor4
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor4.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor4.setOnMouseClicked(
        e -> {
          currentFloor = 4;
          setFloorImg("/edu/wpi/N/images/map/Floor4SolidBackground.png");
          setFloorButtonColors();
        });
    btn_floor5
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor5.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor5.setOnMouseClicked(
        e -> {
          currentFloor = 5;
          setFloorImg("/edu/wpi/N/images/map/Floor5TeamN.png");
          setFloorButtonColors();
        });
    btn_floor6
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor6.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor6.setOnMouseClicked(
        e -> {
          currentFloor = 1;
          currentBuilding = "main";
          setMainCampusDefaults();
          setFloorImg("/edu/wpi/N/images/map/MainL2.png");
          setFloorButtonColorsM();
          nodesListF.setVisible(false);
          nodesListM.setVisible(true);
        });
  }
   */

  /*
  public void initializeMainCampusFloorButtons() {
    btn_floorsM = new JFXButton("Floors");
    btn_floor1M = new JFXButton("L2");
    btn_floor2M = new JFXButton("L1");
    btn_floor3M = new JFXButton("G");
    btn_floor4M = new JFXButton("1");
    btn_floor5M = new JFXButton("2");
    btn_floor6M = new JFXButton("3");

    btn_floor7M = new JFXButton("Switch");
    btn_floorsM.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor1M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor2M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor3M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor4M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor5M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor6M.setButtonType(JFXButton.ButtonType.RAISED);
    btn_floor7M.setButtonType(JFXButton.ButtonType.RAISED);
    nodesListM = new JFXNodesList();
    nodesListM.addAnimatedNode(btn_floorsM);
    nodesListM.addAnimatedNode(btn_floor6M);
    nodesListM.addAnimatedNode(btn_floor5M);
    nodesListM.addAnimatedNode(btn_floor4M);
    nodesListM.addAnimatedNode(btn_floor3M);
    nodesListM.addAnimatedNode(btn_floor2M);
    nodesListM.addAnimatedNode(btn_floor1M);
    nodesListM.addAnimatedNode(btn_floor7M);

    nodesListM.setSpacing(10);
    pn_changeFloor.getChildren().add(nodesListM);
    nodesListM.setVisible(false);
    btn_floorsM
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floorsM.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor1M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor1M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor1M.setOnMouseClicked(
        e -> {
          currentFloor = 1;
          setFloorImg("/edu/wpi/N/images/map/MainL2.png");
          setFloorButtonColorsM();
        });
    btn_floor2M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor2M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor2M.setOnMouseClicked(
        e -> {
          currentFloor = 2;
          setFloorImg("/edu/wpi/N/images/map/MainL1.png");
          setFloorButtonColorsM();
        });
    btn_floor3M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor3M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor3M.setOnMouseClicked(
        e -> {
          currentFloor = 3;
          setFloorImg("/edu/wpi/N/images/map/MainGround.png");
          setFloorButtonColorsM();
        });
    btn_floor4M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor4M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor4M.setOnMouseClicked(
        e -> {
          currentFloor = 4;
          setFloorImg("/edu/wpi/N/images/map/MainResizedF1.png");
          setFloorButtonColorsM();
        });
    btn_floor5M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor5M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor5M.setOnMouseClicked(
        e -> {
          currentFloor = 5;
          setFloorImg("/edu/wpi/N/images/map/MainFloor2.png");
          setFloorButtonColorsM();
        });
    btn_floor6M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor6M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor6M.setOnMouseClicked(
        e -> {
          currentFloor = 6;
          setFloorImg("/edu/wpi/N/images/map/MainFloor3.png");
          setFloorButtonColorsM();
        });
    btn_floor7M
        .getStylesheets()
        .addAll(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn_floor7M.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
    btn_floor7M.setOnMouseClicked(
        e -> {
          currentFloor = 1;
          currentBuilding = "Faulkner";
          setFaulknerDefaults();
          nodesListM.setVisible(false);
          nodesListF.setVisible(true);
          setFloorImg("/edu/wpi/N/images/map/Floor1Reclor.png");
          setFloorButtonColors();
        });
  }

   */

  /*
  private void setFloorButtonColorsM() {
    if (currentFloor == 1) {
      btn_floor1M.setStyle("-fx-background-color: #F7B80F");
      btn_floor2M.setStyle("-fx-background-color: #002186");
      btn_floor3M.setStyle("-fx-background-color: #002186");
      btn_floor4M.setStyle("-fx-background-color: #002186");
      btn_floor5M.setStyle("-fx-background-color: #002186");
      btn_floor6M.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 2) {
      btn_floor1M.setStyle("-fx-background-color: #002186");
      btn_floor2M.setStyle("-fx-background-color: #F7B80F");
      btn_floor3M.setStyle("-fx-background-color: #002186");
      btn_floor4M.setStyle("-fx-background-color: #002186");
      btn_floor5M.setStyle("-fx-background-color: #002186");
      btn_floor6M.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 3) {
      btn_floor1M.setStyle("-fx-background-color: #002186");
      btn_floor2M.setStyle("-fx-background-color: #002186");
      btn_floor3M.setStyle("-fx-background-color: #F7B80F");
      btn_floor4M.setStyle("-fx-background-color: #002186");
      btn_floor5M.setStyle("-fx-background-color: #002186");
      btn_floor6M.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 4) {
      btn_floor1M.setStyle("-fx-background-color: #002186");
      btn_floor2M.setStyle("-fx-background-color: #002186");
      btn_floor3M.setStyle("-fx-background-color: #002186");
      btn_floor4M.setStyle("-fx-background-color: #F7B80F");
      btn_floor5M.setStyle("-fx-background-color: #002186");
      btn_floor6M.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 5) {
      btn_floor1M.setStyle("-fx-background-color: #002186");
      btn_floor2M.setStyle("-fx-background-color: #002186");
      btn_floor3M.setStyle("-fx-background-color: #002186");
      btn_floor4M.setStyle("-fx-background-color: #002186");
      btn_floor5M.setStyle("-fx-background-color: #F7B80F");
      btn_floor6M.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 6) {
      btn_floor1M.setStyle("-fx-background-color: #002186");
      btn_floor2M.setStyle("-fx-background-color: #002186");
      btn_floor3M.setStyle("-fx-background-color: #002186");
      btn_floor4M.setStyle("-fx-background-color: #002186");
      btn_floor5M.setStyle("-fx-background-color: #002186");
      btn_floor6M.setStyle("-fx-background-color: #F7B80F");
    }
  }

  private void setFloorButtonColors() {
    if (currentFloor == 1) {
      btn_floor1.setStyle("-fx-background-color: #F7B80F");
      btn_floor2.setStyle("-fx-background-color: #002186");
      btn_floor3.setStyle("-fx-background-color: #002186");
      btn_floor4.setStyle("-fx-background-color: #002186");
      btn_floor5.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 2) {
      btn_floor1.setStyle("-fx-background-color: #002186");
      btn_floor2.setStyle("-fx-background-color: #F7B80F");
      btn_floor3.setStyle("-fx-background-color: #002186");
      btn_floor4.setStyle("-fx-background-color: #002186");
      btn_floor5.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 3) {
      btn_floor1.setStyle("-fx-background-color: #002186");
      btn_floor2.setStyle("-fx-background-color: #002186");
      btn_floor3.setStyle("-fx-background-color: #F7B80F");
      btn_floor4.setStyle("-fx-background-color: #002186");
      btn_floor5.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 4) {
      btn_floor1.setStyle("-fx-background-color: #002186");
      btn_floor2.setStyle("-fx-background-color: #002186");
      btn_floor3.setStyle("-fx-background-color: #002186");
      btn_floor4.setStyle("-fx-background-color: #F7B80F");
      btn_floor5.setStyle("-fx-background-color: #002186");
    } else if (currentFloor == 5) {
      btn_floor1.setStyle("-fx-background-color: #002186");
      btn_floor2.setStyle("-fx-background-color: #002186");
      btn_floor3.setStyle("-fx-background-color: #002186");
      btn_floor4.setStyle("-fx-background-color: #002186");
      btn_floor5.setStyle("-fx-background-color: #F7B80F");
    }
  }

   */

  private void setFloorImg(String path) {
    resetAllExceptShafts();
    hideEditElevButton();
    Image img = new Image(getClass().getResourceAsStream(path));
    img_map.setImage(img);
    try {
      loadFloor();
    } catch (DBException | IOException e) {
      e.printStackTrace();
    }
  }

  public void onBtnCancelElevClicked() {
    resetEditElev();
    mode = Mode.NO_STATE;
    // btn_cancel_elev.setDisable(true);
    btn_cancel_elev.setVisible(false);
  }

  public void hideEditElevButton() {
    // btn_cancel_elev.setDisable(true);
    btn_cancel_elev.setVisible(false);
  }

  public void reloadAddShaft() throws DBException {
    mode = Mode.ADD_SHAFT;
    try {
      changeEditor();
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (DbNode c : addShaftNodeCircles) { // TODO: Null pointer when remove
      controllerAddShaft.addLstAddShaftNode(c.getLongName());
      if (c.getFloor() == currentFloor) {
        nodesMap2.get(c).setFill(Color.BLACK); // null pointer here, nodesmap not set??
      }
    }
    pn_editor.setVisible(true);
    // pn_elev.setDisable(true);
    onBtnCancelAddShaftClicked();
    onBtnConfirmAddShaftClicked();
    for (Circle c : editElevNodes) {
      if (!(c.getFill() == Color.BLACK)) c.setFill(Color.CADETBLUE);
    }
  }

  public boolean isInShaft(DbNode node) throws DBException {
    LinkedList<LinkedList<DbNode>> allShafts = MapDB.getShafts(currentBuilding);
    for (LinkedList<DbNode> nodesInShafts : allShafts) {
      if (nodesInShafts.contains(node)) {
        return true;
      }
    }
    return false;
  }

  //   == MAP ZOOM CONTROLS ==

  // When user scrolls mouse over map
  @FXML
  private void mapScrollHandler(ScrollEvent event) throws IOException {
    if (event.getSource() == pn_stack) {
      double deltaY = event.getDeltaY();
      zoom(deltaY * ZOOM_STEP_SCROLL);
    }
  }

  /**
   * zoom - Scale map pane up or down, clamping value between MIN_MAP_SCALE and MAX_MAP_SCALE
   *
   * @param percentDelta - Signed double representing how much to zoom in/out
   */
  private void zoom(double percentDelta) {

    // Scaling parameter (alpha) is clamped between 0 (min. scale) and 1 (max. scale)
    mapScaleAlpha.set(
        Math.max(
            0,
            Math.min(
                1,
                mapScaleAlpha.get() + percentDelta))); // TODO: use zoom to scale line & node size

    // Linearly interpolate (lerp) alpha to actual scale value
    double lerpedScale = MIN_MAP_SCALE + mapScaleAlpha.get() * (MAX_MAP_SCALE - MIN_MAP_SCALE);

    // Apply new scale and correct panning
    pn_stack.setScaleX(lerpedScale);
    pn_stack.setScaleY(lerpedScale);
    clampPanning(0, 0);
  }

  // == MAP PANNING CONTROLS ==

  // User begins drag
  @FXML
  private void mapPressHandler(MouseEvent event) throws IOException {
    if (!isDraggingNode && event.getSource() == pn_stack) {
      pn_stack.setCursor(Cursor.CLOSED_HAND);
      clickStartX = event.getSceneX();
      clickStartY = event.getSceneY();
    }
  }

  // User is currently dragging
  @FXML
  private void mapDragHandler(MouseEvent event) throws IOException {
    if (!isDraggingNode && event.getSource() == pn_stack) {

      double dragDeltaX = event.getSceneX() - clickStartX;
      double dragDeltaY = event.getSceneY() - clickStartY;

      clampPanning(dragDeltaX, dragDeltaY);

      clickStartX = event.getSceneX();
      clickStartY = event.getSceneY();
    }
  }

  // User ends drag
  @FXML
  private void mapReleaseHandler(MouseEvent event) throws IOException {
    pn_stack.setCursor(Cursor.OPEN_HAND);
  }

  /**
   * clampPanning - Attempts to move map by deltaX and deltaY, clamping movement to stay in-bounds
   *
   * @param deltaX - How many screen pixels to move the map horizontally
   * @param deltaY - How many screen pixels to move the map vertically
   */
  private void clampPanning(double deltaX, double deltaY) {
    double xLimit = (pn_stack.getScaleX() - MIN_MAP_SCALE) * MAP_WIDTH / 2;
    double yLimit = (pn_stack.getScaleY() - MIN_MAP_SCALE) * MAP_HEIGHT / 2;

    double newTranslateX = Math.min(Math.max(pn_stack.getTranslateX() + deltaX, -xLimit), xLimit);
    double newTranslateY = Math.min(Math.max(pn_stack.getTranslateY() + deltaY, -yLimit), yLimit);

    pn_stack.setTranslateX(newTranslateX);
    pn_stack.setTranslateY(newTranslateY);
  }

  private void initAutoFocus() {
    autoFocus.setCycleCount(1);
    autoFocus.setOnFinished(event -> onAutoFocusEnd());
  }

  private void autoFocusToNode(DbNode node) {
    autoFocusToPoint(node.getX(), node.getY());
  }

  private void autoFocusToNodesGroup(LinkedList<Circle> circles) {

    double xSum = 0;
    double ySum = 0;
    int nodesCount = circles.size();

    for (Circle circle : circles) {
      xSum += circle.getCenterX();
      ySum += circle.getCenterY();
    }

    autoFocusToPoint(xSum / nodesCount, ySum / nodesCount);
  }

  private void autoFocusToPoint(double x, double y) {

    endFocusVals.add(new KeyValue(pn_stack.scaleXProperty(), MAX_MAP_SCALE, Interpolator.LINEAR));
    endFocusVals.add(new KeyValue(pn_stack.scaleYProperty(), MAX_MAP_SCALE, Interpolator.LINEAR));
    endFocusVals.add(
        new KeyValue(
            pn_stack.translateXProperty(),
            MAX_MAP_SCALE * (MAP_WIDTH / 2 - x),
            Interpolator.LINEAR));
    endFocusVals.add(
        new KeyValue(
            pn_stack.translateYProperty(),
            MAX_MAP_SCALE * (MAP_HEIGHT / 2 - y),
            Interpolator.LINEAR));
    endFocusVals.add(new KeyValue(mapScaleAlpha, 1, Interpolator.LINEAR));

    KeyFrame endFrame = new KeyFrame(Duration.millis(500), "endFocus", null, endFocusVals);

    autoFocus.getKeyFrames().setAll(endFrame);
    autoFocus.playFromStart();
  }

  private void onAutoFocusEnd() {
    autoFocus.stop();
    autoFocus.getKeyFrames().clear();
    endFocusVals.clear();
  }

  //////////////

  /**
   * styles the switch, faulkner, main, and driving directions buttons
   *
   * @param btn the building button which is to be styled
   */
  public void styleBuildingButtons(JFXButton btn) {
    btn.getStylesheets()
        .add(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn.getStyleClass().add("header-button");
  }

  /**
   * styles the sub buttons of the building buttons
   *
   * @param btn the floor button which is to be styled
   */
  public void styleFloorButtons(JFXButton btn) {
    btn.getStylesheets()
        .add(getClass().getResource("/edu/wpi/N/css/MapDisplayFloors.css").toExternalForm());
    btn.getStyleClass().add("choice-button");
  }

  /**
   * initializes all buttons required to switch between floors
   *
   * @throws DBException
   */
  public void initFloorButtons() throws DBException {
    // Building Buttons
    JFXButton btn_buildings = new JFXButton("Switch Map");
    styleBuildingButtons(btn_buildings);
    JFXButton btn_faulkner = new JFXButton("Faulkner");
    styleBuildingButtons(btn_faulkner);
    JFXButton btn_main = new JFXButton("Main");
    styleBuildingButtons(btn_main);

    // Faulkner Buttons
    JFXButton btn_faulkner1 = new JFXButton("F1");
    styleFloorButtons(btn_faulkner1);

    JFXButton btn_faulkner2 = new JFXButton("F2");
    styleFloorButtons(btn_faulkner2);
    JFXButton btn_faulkner3 = new JFXButton("F3");
    styleFloorButtons(btn_faulkner3);
    JFXButton btn_faulkner4 = new JFXButton("F4");
    styleFloorButtons(btn_faulkner4);
    JFXButton btn_faulkner5 = new JFXButton("F5");
    styleFloorButtons(btn_faulkner5);
    faulknerButtonList
        .getChildren()
        .addAll(
            btn_faulkner,
            btn_faulkner1,
            btn_faulkner2,
            btn_faulkner3,
            btn_faulkner4,
            btn_faulkner5);

    // Main Buttons
    JFXButton btn_main1 = new JFXButton("L2");
    styleFloorButtons(btn_main1);
    JFXButton btn_main2 = new JFXButton("L1");
    styleFloorButtons(btn_main2);
    JFXButton btn_main3 = new JFXButton("G");
    styleFloorButtons(btn_main3);
    JFXButton btn_main4 = new JFXButton("1");
    styleFloorButtons(btn_main4);
    JFXButton btn_main5 = new JFXButton("2");
    styleFloorButtons(btn_main5);
    JFXButton btn_main6 = new JFXButton("3");
    styleFloorButtons(btn_main6);

    // Set onClick properties
    onFloorButtonClicked(btn_faulkner1);
    onFloorButtonClicked(btn_faulkner2);
    onFloorButtonClicked(btn_faulkner3);
    onFloorButtonClicked(btn_faulkner4);
    onFloorButtonClicked(btn_faulkner5);
    onFloorButtonClicked(btn_main1);
    onFloorButtonClicked(btn_main2);
    onFloorButtonClicked(btn_main3);
    onFloorButtonClicked(btn_main4);
    onFloorButtonClicked(btn_main5);
    onFloorButtonClicked(btn_main6);

    mainButtonList
        .getChildren()
        .addAll(btn_main, btn_main1, btn_main2, btn_main3, btn_main4, btn_main5, btn_main6);
    buildingButtonList.addAnimatedNode(btn_buildings);
    buildingButtonList.addAnimatedNode(faulknerButtonList);
    buildingButtonList.addAnimatedNode(mainButtonList);

    buildingButtonList.setSpacing(100);
    buildingButtonList.setRotate(90);
    faulknerButtonList.setSpacing(15);
    mainButtonList.setSpacing(15);

    pn_floors.getChildren().add(buildingButtonList);
  }

  /**
   * initializes a listener for a floor button click event
   *
   * @param btn the floor button which is clicked
   * @throws DBException
   */
  public void onFloorButtonClicked(JFXButton btn) throws DBException {
    String txt = btn.getText();
    btn.setOnMouseClicked(
        e -> {
          try {
            handleFloorButtonClicked(txt);
          } catch (DBException ex) {
            ex.printStackTrace();
          }
        });
  }

  /**
   * handles the event of a button click
   *
   * @param txt the text of the button which is clicked
   * @throws DBException
   */
  public void handleFloorButtonClicked(String txt) throws DBException {
    collapseAllFloorButtons();
    if (txt.equals("F1")) {
      changeFloor(1, "Faulkner");
      this.currentFloor = 1;
      this.currentBuilding = "Faulkner";
      setFloorImg("/edu/wpi/N/images/map/Floor1Reclor.png");
    } else if (txt.equals("F2")) {
      changeFloor(2, "Faulkner");
      this.currentFloor = 2;
      this.currentBuilding = "Faulkner";
      setFloorImg("/edu/wpi/N/images/map/Floor2TeamN.png");
    } else if (txt.equals("F3")) {
      changeFloor(3, "Faulkner");
      this.currentFloor = 3;
      this.currentBuilding = "Faulkner";
      setFloorImg("/edu/wpi/N/images/map/Floor3TeamN.png");
    } else if (txt.equals("F4")) {
      changeFloor(4, "Faulkner");
      this.currentFloor = 4;
      this.currentBuilding = "Faulkner";
      setFloorImg("/edu/wpi/N/images/map/Floor4SolidBackground.png");
    } else if (txt.equals("F5")) {
      changeFloor(5, "Faulkner");
      this.currentFloor = 5;
      this.currentBuilding = "Faulkner";
      setFloorImg("/edu/wpi/N/images/map/Floor5TeamN.png");
    } else if (txt.equals("L2")) {
      changeFloor(1, "Main");
      this.currentFloor = 1;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainL2.png");
    } else if (txt.equals("L1")) {
      changeFloor(2, "Main");
      this.currentFloor = 2;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainL1.png");

    } else if (txt.equals("G")) {
      changeFloor(3, "Main");
      this.currentFloor = 3;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainGround.png");
    } else if (txt.equals("1")) {
      changeFloor(4, "Main");
      this.currentFloor = 4;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainResizedF1.png");
    } else if (txt.equals("2")) {
      changeFloor(5, "Main");
      this.currentFloor = 5;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainFloor2.png");

    } else if (txt.equals("3")) {
      changeFloor(6, "Main");
      this.currentFloor = 6;
      this.currentBuilding = "Main";
      setFloorImg("/edu/wpi/N/images/map/MainFloor3.png");
    }
  }

  /**
   * switches the current floor displayed on the map
   *
   * @param newFloor the new floor
   * @param newBuilding the new building
   * @throws DBException
   */
  public void changeFloor(int newFloor, String newBuilding) throws DBException {
    this.currentFloor = newFloor;
    this.currentBuilding = newBuilding;
    setFloorBuildingText(this.currentFloor, this.currentBuilding);
    if (this.currentBuilding.equals("Main")) {
      setMainCampusDefaults();
    } else {
      setFaulknerDefaults();
    }
  }

  /** collapses all the floor buttons back to the main button */
  public void collapseAllFloorButtons() {
    buildingButtonList.animateList(false);
    mainButtonList.animateList(false);
    faulknerButtonList.animateList(false);
  }

  /**
   * displays the label text
   *
   * @param floor the current floor
   * @param building the current building
   */
  public void setFloorBuildingText(int floor, String building) {
    if (floor == -1) {
      lbl_building_floor.setText("Driving Directions");
    }
    if (!building.equals("Faulkner")) {
      if (floor == 1) {
        lbl_building_floor.setText(building + ", " + "L2");
      } else if (floor == 2) {
        lbl_building_floor.setText(building + ", " + "L1");
      }
      if (floor == 3) {
        lbl_building_floor.setText(building + ", " + "G");
      }
      if (floor == 4) {
        lbl_building_floor.setText(building + ", " + "1");
      }
      if (floor == 5) {
        lbl_building_floor.setText(building + ", " + "2");
      }
      if (floor == 6) {
        lbl_building_floor.setText(building + ", " + "3");
      }
    } else {
      lbl_building_floor.setText(building + ", " + floor);
    }
  }
}
