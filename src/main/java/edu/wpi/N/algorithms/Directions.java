package edu.wpi.N.algorithms;

import static edu.wpi.N.algorithms.Directions.State.*;
import static edu.wpi.N.algorithms.Level.*;
import static java.lang.Math.atan2;

import edu.wpi.N.database.DBException;
import edu.wpi.N.entities.DbNode;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Directions {
  private ArrayList<Direction> directions;
  private static ArrayList<DbNode> path;
  private static State state;
  private static final double TURN_THRESHOLD = 45;
  private static final double SLIGHT_TURN_THRESHOLD = 20;
  private static final double SHARP_TURN_THRESHOLD = 95;
  private static Icon currIcon;

  enum State {
    STARTING,
    CONTINUING,
    TURNING,
    CHANGING_FLOOR,
    ARRIVING
  }

  public Directions(LinkedList<DbNode> path) {
    this.directions = new ArrayList<>();
    ArrayList<DbNode> pathNodes = new ArrayList<DbNode>();
    for (DbNode node : path) {
      pathNodes.add(node);
    }
    this.path = pathNodes;
  }

  /** Generates textual directions for the given path */
  private void generateDirections(HashMap<String, LinkedList<DbNode>> mapDatas, Boolean driving)
      throws DBException {
    DbNode currNode;
    DbNode nextNode = path.get(0);
    // DbNode endOfHallNode = null;
    double distance = 0;
    boolean stateChange = true;
    double angle = 0;
    String startFloor = "";
    DbNode startFloorNode = path.get(0);
    String message = "";
    boolean messageCheck = false;
    double totalDistance = 0;
    double totalTime = 0;
    currIcon = Icon.CONTINUE;
    for (int i = 0; i <= path.size() - 1; i++) {
      currNode = path.get(i);
      if (i < path.size() - 1) {
        nextNode = path.get(i + 1);
        angle = getAngle(i);
        stateChange = !getState(i + 1).equals(state);
        totalDistance += getDistance(path.get(i), path.get(i + 1));
      }
      if (i > 0) addFloorLevelDirection(i - 1);
      state = getState(i);
      switch (state) {
        case STARTING:
          if (currNode.getNodeType().equals("EXIT")) {
            message = "Enter at " + getLandmark(currNode, mapDatas).getLongName();
            currIcon = Icon.ENTER;
          } else if (currNode.getNodeID().equals("NSERV00301")
              || currNode.getNodeID().equals("NSERV00103")) {
            message = "Start in the direction of the kiosk arrow ";
          } else if (!path.get(0).getNodeType().equals("HALL")) {
            message = "Exit " + path.get(0).getLongName();
            currIcon = Icon.EXIT;
          } else if (!(getLandmark(nextNode, mapDatas) == null)) {
            message =
                "Start towards "
                    + getLandmark(nextNode, mapDatas).getLongName()
                    + " "
                    + getDistanceString(getDistance(currNode, nextNode));
          } else {
            message =
                "Start by proceeding down the hall "
                    + getDistanceString(getDistance(currNode, nextNode));
          }
          break;
        case CONTINUING:
          distance += getDistance(currNode, nextNode);
          //          if (endOfHallNode == null) {
          //            endOfHallNode = findEndOfHall(i);
          //          }
          if (!message.equals("") && i == 1) {
            // currIcon = Icon.CONTINUE;
            directions.add(new Direction(message, STEP, currNode, currIcon));
            message = "";
          } else if (getState(i - 1).equals(CHANGING_FLOOR)) {
            message = "Exit " + currNode.getLongName();
            currIcon = Icon.EXIT;
          } else if (stateChange || atIntersection(currNode, mapDatas)) {
            if (getLandmark(nextNode, mapDatas) == null) {
              if (currNode.getBuilding().equals("Faulkner"))
                message = "Continue to next intersection " + getDistanceString(distance);

            } else if (getLandmark(nextNode, mapDatas).equals(nextNode)) {
              message =
                  "Go towards " // "Proceed straight towards "
                      + getLandmark(nextNode, mapDatas).getLongName()
                      + " "
                      + getDistanceString(distance);
            } else {
              message =
                  "Continue past "
                      + getLandmark(nextNode, mapDatas).getLongName()
                      + " "
                      + getDistanceString(distance);
            }
            distance = 0;
          }
          break;
        case TURNING:
          if (!nextNode.equals(path.get(path.size() - 1))) {
            if (!message.equals("")) {
              message = message + " and t" + getTurnType(angle, getAngle(i - 1));
              directions.add(new Direction(message, STEP, currNode, currIcon));
              message = "";
            } else if (!(getLandmark(currNode, mapDatas) == null)) {
              message =
                  "Go towards straight "
                      + getLandmark(currNode, mapDatas).getLongName()
                      + " "
                      + getDistanceString(getDistance(currNode, nextNode))
                      + " and t"
                      + getTurnType(angle, getAngle(i - 1))
                      + " at the intersection";
              directions.add(new Direction(message, STEP, currNode, currIcon));
              message = "";
            } else {
              if (distance == 0) {
                message =
                    "At the next intersection "
                        + getDistanceString(getDistance(currNode, nextNode))
                        + " t"
                        + getTurnType(angle, getAngle(i - 1));
                directions.add(new Direction(message, STEP, currNode, currIcon));
                message = "";
              } else {
                message =
                    "At the next intersection "
                        + getDistanceString(distance)
                        + " t"
                        + getTurnType(angle, getAngle(i - 1));
                directions.add(new Direction(message, STEP, currNode, currIcon));
                message = "";
              }
            }
          }
          break;
        case CHANGING_FLOOR:
          totalTime += 37; // add 37 sec for average floor change time
          if (!getState(i - 1).equals(CHANGING_FLOOR)) {
            startFloor = currNode.getLongName();
            startFloorNode = currNode;
          }
          if (!message.equals("")) {
            message = message + " and enter " + currNode.getLongName();
            directions.add(new Direction(message, STEP, startFloorNode, currIcon));
            message = "";
            messageCheck = true;
          }
          if (path.get(i).getNodeType().equals("STAI")) currIcon = Icon.STAIR;
          else if (path.get(i).getNodeType().equals("ELEV")) currIcon = Icon.ELEVATOR;
          if (stateChange && getState(i - 1).equals(CHANGING_FLOOR)) {
            if (messageCheck) {
              message = "Take " + startFloor + " to floor " + currNode.getFloor();
              directions.add(new Direction(message, STEP, startFloorNode, currIcon));
              message = "";
              messageCheck = false;
            } else {
              message = "Enter " + startFloor + " and go to floor " + currNode.getFloor();
              directions.add(new Direction(message, STEP, startFloorNode, currIcon));
              message = "";
            }
          }
          break;
        case ARRIVING:
          if (currNode.getNodeType().equals("EXIT")) {
            if (!message.equals("")) {
              message = message + " and exit at " + currNode.getLongName();
              currIcon = Icon.EXIT;
              directions.add(new Direction(message, STEP, currNode, currIcon));
              message = "";
            } else {
              message = "Exit at " + currNode.getLongName();
              currIcon = Icon.EXIT;
              directions.add(new Direction(message, STEP, currNode, currIcon));
              message = "";
            }
          } else if (getState(i - 1).equals(TURNING)) {
            String turnMessage = "T" + getTurnType(angle, getAngle(i - 2));
            message =
                turnMessage
                    + " and arrive at "
                    + currNode.getLongName()
                    + " "
                    + getTotalTimeString(totalDistance, totalTime);
            currIcon = Icon.ARRIVE;
            directions.add(new Direction(message, STEP, currNode, currIcon));
            message = "";
          } else if (!message.equals("")) {
            message =
                message
                    + " and arrive at destination "
                    + getTotalTimeString(totalDistance, totalTime);
            currIcon = Icon.ARRIVE;
            directions.add(new Direction(message, STEP, currNode, currIcon));
            message = "";
          } else if (currNode.getNodeType().equals("EXIT")) {
            message = "Exit " + currNode.getLongName();
            currIcon = Icon.EXIT;
            directions.add(new Direction(message, STEP, currNode, currIcon));
            message = "";
          } else {
            message =
                "Arrive at "
                    + currNode.getLongName()
                    + " "
                    + getTotalTimeString(totalDistance, totalTime);
            currIcon = Icon.ARRIVE;
            directions.add(new Direction(message, STEP, currNode, currIcon));
            message = "";
          }
          if (driving) {
            if (path.get(i).getBuilding().equals("Faulkner")) {
              directions.add(
                  new Direction("Drive to Main Campus", DRIVING, currNode, Icon.DRIVING));
            } else {
              directions.add(new Direction("Drive to Faulkner", DRIVING, currNode, Icon.DRIVING));
            }
          }
          break;
      }
    }
  }

  /**
   * gets string for total time using average walking speed of 4.6 ft/s and avg elevator ride of 37
   * sec
   *
   * @param totalDistance
   * @param time
   * @return String
   */
  public static String getTotalTimeString(double totalDistance, double time) {
    int totalTime = (int) Math.round((totalDistance / 4.6 + time) / 60);
    if (totalTime <= 0) {
      return "(Est. less than 1 min)";
    } else if (totalTime == 1) {
      return "(Est." + totalTime + " min)";
    } else {
      return "(Est." + totalTime + " min)";
    }
  }

  private static DbNode findEndOfHall(int index, HashMap<String, LinkedList<DbNode>> mapDatas)
      throws DBException {
    double angleChange;
    boolean endOfHall = false;
    while (getState(index).equals(CONTINUING)
        && index < path.size()) { // || (getState(index - 1).equals(CONTINUING)
      for (DbNode adj : mapDatas.get(path.get(index).getNodeID())) {
        if (adj.getNodeType().equals("HALL")) {
          angleChange = getAngle(index, adj) - getAngle(index - 1);
          if (angleChange > 180) {
            angleChange -= 360;
          } else if (angleChange < -180) {
            angleChange += 360;
          }
          if (Math.abs(angleChange) < SLIGHT_TURN_THRESHOLD && !adj.equals(path.get(index - 1))) {
            endOfHall = false;
            break;
          } else {
            endOfHall = true;
          }
        }
      }
      if (endOfHall) {
        // System.out.println(path.get(index));
        return path.get(index);
      }
      index++;
    }
    return null;
  }

  /**
   * gets the state based off of currNode and change in angle
   *
   * @return int, State
   */
  private static State getState(int i) {
    if (i == 0) {
      return STARTING;
    } else if (i == path.size() - 1) {
      return ARRIVING;
    } else if ((path.get(i).getNodeType().equals("ELEV")
            || path.get(i).getNodeType().equals("STAI"))
        && (path.get(i).getFloor() != path.get(i + 1).getFloor()
            || path.get(i).getNodeType().equals(path.get(i - 1).getNodeType()))) {
      return CHANGING_FLOOR;
    } else if (Math.abs(getAngle(i) - getAngle(i - 1)) > SLIGHT_TURN_THRESHOLD
        && Math.abs(getAngle(i) - getAngle(i - 1)) < 360 - SLIGHT_TURN_THRESHOLD) {
      return TURNING;
    } else {
      return CONTINUING;
    }
  }

  private void addFloorLevelDirection(int i) {
    if (i == 0) {
      directions.add(
          new Direction(getFloorString(path.get(i)), Level.FLOOR, path.get(i), Icon.FLOOR_LEVEL));
    } else if (path.get(i - 1).getFloor() != path.get(i).getFloor()
        && !path.get(i + 1).getNodeType().equals(path.get(i).getNodeType())) {
      directions.add(
          new Direction(getFloorString(path.get(i)), Level.FLOOR, path.get(i), Icon.FLOOR_LEVEL));
    }
  }

  private String getFloorString(DbNode n) {
    if (n.getBuilding().equals("Faulkner")) {
      if (n.getFloor() == 1) return "First floor";
      if (n.getFloor() == 2) return "Second floor";
      if (n.getFloor() == 3) return "Third floor";
      if (n.getFloor() == 4) return "Fourth floor";
      if (n.getFloor() == 5) return "Fifth floor";
    } else {
      if (n.getFloor() == 1) return "Lower Level Two";
      if (n.getFloor() == 2) return "Lower Level One";
      if (n.getFloor() == 3) return "Ground Floor";
      if (n.getFloor() == 4) return "First floor";
      if (n.getFloor() == 5) return "Second floor";
      if (n.getFloor() == 6) return "Third floor";
    }
    return "Unknown Floor Number";
  }

  /**
   * Takes a current node and next node and returns the type of turn
   *
   * @param angle, double change in angle
   * @return String, the type of turn (right, left, straight)
   */
  private static String getTurnType(double angle, double prevAngle) {
    double angleChange = angle - prevAngle;
    if (angleChange > 180) {
      angleChange -= 360;
    } else if (angleChange < -180) {
      angleChange += 360;
    }
    // System.out.println(angleChange);
    if (angleChange <= TURN_THRESHOLD && angleChange >= SLIGHT_TURN_THRESHOLD) {
      currIcon = Icon.SLIGHT_RIGHT;
      return "ake a slight right";
    } else if (angleChange > SHARP_TURN_THRESHOLD) {
      currIcon = Icon.SHARP_RIGHT;
      return "ake a sharp right turn";
    } else if (angleChange >= TURN_THRESHOLD) {
      currIcon = Icon.RIGHT;
      return "urn right";
    } else if (angleChange >= -1 * TURN_THRESHOLD && angleChange <= -1 * SLIGHT_TURN_THRESHOLD) {
      currIcon = Icon.SLIGHT_LEFT;
      return "ake a slight left";
    } else if (angleChange <= -1 * SHARP_TURN_THRESHOLD) {
      currIcon = Icon.SHARP_LEFT;
      return "ake a sharp left turn";
    } else if (angleChange <= -1 * TURN_THRESHOLD) {
      currIcon = Icon.LEFT;
      return "urn left";
    } else {
      currIcon = Icon.CONTINUE;
      return "straight" + angleChange;
    }
  }

  /**
   * Gets closest landmark to a node, if the node is a HALL node, returns an adjacent non-hallway
   * node or null if node is not a HALL node, returns the same node
   *
   * @param node, DbNode
   * @return String, landmark for given node
   */
  private static DbNode getLandmark(DbNode node, HashMap<String, LinkedList<DbNode>> mapDatas)
      throws DBException {
    if (node.getNodeType().equals("HALL")) {
      for (DbNode n : mapDatas.get(node.getNodeID())) {
        if (!n.getNodeType().equals("HALL")
            && !n.getNodeType().equals("ELEV")
            && !n.getNodeType().equals("STAI")) {
          return n;
        }
      }
      return null;
    }
    return node;
  }

  /**
   * Generates string (Go X ft) rounds distance to whole number
   *
   * @param distance, end, dbNode start and end
   * @return String, how far in feet between nodes
   */
  private static String getDistanceString(double distance) {
    return "(" + Math.round(distance) + " ft)"; // "(Go " + Math.round(distance) + " ft)"
  }

  /**
   * gets the angle between two nodes use atan2
   *
   * @return double, angle
   */
  private static double getAngle(int i) {
    double dy = path.get(i + 1).getY() - path.get(i).getY();
    double dx = path.get(i + 1).getX() - path.get(i).getX();
    return Math.toDegrees(atan2(dy, dx));
  }

  /**
   * gets the angle between two nodes use atan2
   *
   * @return double, angle
   */
  private static double getAngle(int i, DbNode n) {
    double dy = n.getY() - path.get(i).getY();
    double dx = n.getX() - path.get(i).getX();
    return Math.toDegrees(atan2(dy, dx));
  }

  /**
   * calculates the distance between two nodes using appropriate conversion factor
   *
   * @param currNode, current DbNode
   * @param nextNode, next DbNode
   * @return double, distance between current node and next node
   */
  private static double getDistance(DbNode currNode, DbNode nextNode) {
    double distance =
        Math.sqrt(
            Math.pow(nextNode.getX() - currNode.getX(), 2)
                + Math.pow(nextNode.getY() - currNode.getY(), 2));
    double conversion = 0.338;
    return distance * conversion;
  }

  /**
   * Determines if a node is at an intersection by looking for adjacent hallway nodes
   *
   * @param node, DbNode
   * @return true, if intersection, false otherwise
   */
  private static boolean atIntersection(DbNode node, HashMap<String, LinkedList<DbNode>> mapDatas)
      throws DBException {
    int hallNodeCount = 0;
    if (node.getNodeType().equals("HALL")) {
      for (DbNode n : mapDatas.get(node.getNodeID())) {
        if (n.getNodeType().equals("HALL")) {
          hallNodeCount++;
        }
      }
      return hallNodeCount >= 3;
    }
    return false;
  }

  /** @return directions with numbers at beginning of each line */
  /*
  public ArrayList<String> getNumberedDirection() {
    ArrayList<String> newDirections = new ArrayList<>();
    int index = 1;
    if (!this.directions.isEmpty()) {
      for (String s : this.directions) {
        newDirections.add(index + ". " + s);
        index++;
      }
    } else {
      return null;
    }
    return newDirections;
  }*/

  /**
   * Takes a path and returns written directions for that path
   *
   * @return ArrayList of strings, each String is a line of directions
   */
  public ArrayList<Direction> getDirections(
      HashMap<String, LinkedList<DbNode>> mapDatas, Boolean driving) throws DBException {
    if (!(this.path == null)) {
      this.generateDirections(mapDatas, driving);
      return this.directions;
    } else {
      return null;
    }
  }

  /**
   * gets the google directions with the specified mode and direction
   *
   * @param urls the request url
   * @return The google directions as an ArrayList of string
   */
  public static ArrayList<String> getGoogleDirectionsStrings(String urls) {

    try {
      URL url = new URL(urls);
      HttpURLConnection httpcon = (HttpURLConnection) (url.openConnection());
      httpcon.setDoOutput(true);
      httpcon.setRequestProperty("Content-Type", "application/json");
      httpcon.setRequestProperty("Accept", "application/json");
      httpcon.setRequestMethod("GET");
      httpcon.connect();
      Scanner sc = new Scanner(url.openStream());
      ArrayList<String> dirs = new ArrayList<>();
      while (sc.hasNext()) {
        String next = sc.nextLine();
        // if (next.contains("\"html_instructions\"")) System.out.println(next);
        if (next.contains("\"html_instructions\""))
          dirs.add(
              next.substring(44)
                  .replace("\\u003cb\\u003e", "")
                  .replace("\\u003c/b\\u003e", "")
                  .replace("\\u003cwbr/\\u003e", " ")
                  .replace("&nbsp;", " ")
                  .replaceAll("(\\\\u003c)(.*?)(\\\\u003e)", "; ")
                  .replace("\",", ""));
        // System.out.println(sc.nextLine() + "K");
      }
      return dirs;
    } catch (MalformedURLException e) {
      // e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ArrayList<Direction> getGoogleDirections(String url) {
    ArrayList<String> directions = getGoogleDirectionsStrings(url);
    ArrayList<Direction> iconDirections = new ArrayList<>();
    if (directions == null) return iconDirections;
    for (int i = 0; i < directions.size(); i++) {
      if (i == directions.size() - 1) {
        iconDirections.add(new Direction(directions.get(i), BUILDING, null, Icon.ARRIVE));
      } else if (directions.get(i).contains("right")) {
        iconDirections.add(new Direction(directions.get(i), BUILDING, null, Icon.RIGHT));
      } else if (directions.get(i).contains("left")) {
        iconDirections.add(new Direction(directions.get(i), BUILDING, null, Icon.LEFT));
      } else if (directions.get(i).contains("circle")) {
        iconDirections.add(new Direction(directions.get(i), BUILDING, null, Icon.CIRCLE));
      } else {
        iconDirections.add(new Direction(directions.get(i), BUILDING, null, Icon.CONTINUE));
      }
    }
    return iconDirections;
  }
}
