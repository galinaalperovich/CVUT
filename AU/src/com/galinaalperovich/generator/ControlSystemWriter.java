package com.galinaalperovich.generator;

import com.galinaalperovich.station.Path;
import com.galinaalperovich.station.Station;
import com.galinaalperovich.station.StationNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlSystemWriter {
  static final String FILE_NAME = "control_system.p";
  private static final String CONFIGURE_PATH = "![Path,Train]: (isConfigured(succ(T),Path) <= (toBeActivated(T,Path)))";
  private Station myStation;

  public ControlSystemWriter(Station station) {
    myStation = station;
  }

  public void write() throws IOException {
    BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME));

    fileWriter.write(String.format("include ('%s').\n", AdditionalConditionsBuilder.FILE_NAME));

    fileWriter.write("%Control system of the station\n\n");

    // -- Edited comment and method after deadline
    fileWriter.write("%Only one entrance can decided if it should opens or not at one moment\n");
    fileWriter.write(generateEntranceDeciding());
    // --

    // -- Added after deadline
    fileWriter.write("%Define state of switches for every time moment\n");
    fileWriter.write(generateSwitchStateRule());
    // --


    //fileWriter.write("%Define the configuration of the switches for the path\n");
    //fileWriter.write(generatePathsConfigured());

    fileWriter.write("%Path is safe for using if there are no trains which can cross the path at some later time\n");
    fileWriter.write(generatePathIsSafe());

    fileWriter.write("%Path is ready to be activated if it is safe, there is a train at the entrance node, it is time to decide for entrance node and train's target is path's exit node\n");
    fileWriter.write(generatePathsToBeActivated());

    //fileWriter.write("%Activate path configuration\n");
    //fileWriter.write(generateConfigurationRule());

    // -- Edited after deadline
    fileWriter.write("%Open the entrance if there is path started with this entrance\n");
    fileWriter.write(generateOpenRule());
    // --

    fileWriter.write("%Path are different\n");
    fileWriter.write(generatePathsAreDifferent());
    fileWriter.close();
  }

  private String generateSwitchStateRule() {
    StringBuilder builder = new StringBuilder();
    List<StationNode> switches = myStation.getStationNodes().stream().filter(StationNode::isSwitch).collect(Collectors.toList());
    for (StationNode currentSwitch : switches) {
      List<StationNode> nextNodes = currentSwitch.getOutgoingNodes();

      for (StationNode nextNode : nextNodes) {
        StringBuilder formulaBuilder = new StringBuilder();
        String switchLabel = currentSwitch.getLabel();
        String nextNodeLabel = nextNode.getLabel();
        String axiomName = String.format("state_switch_%s_to_%s", switchLabel, nextNodeLabel);
        String formulaBody = String.format("(![T]: ?[Train]: ((switch(succ(T), %s) = %s) <=> (", switchLabel, nextNodeLabel);
        formulaBuilder.append(formulaBody);
        List<Path> pathsThrough = myStation.getPathsByNodes(currentSwitch, nextNode);

        formulaBuilder.append(String.format("(at(T,Train,%1$s) & (switch(T, %1$s) = %2$s)) | ", switchLabel, nextNodeLabel));

        boolean firstReached = false;
        for (Path path : pathsThrough) {
          List<StationNode> nodes = path.getNodes();
          StationNode exitNode = nodes.get(nodes.size() - 1);
          int switchIndex = nodes.indexOf(currentSwitch);
          StationNode previousNode = nodes.get(switchIndex - 1);
          if (firstReached) {
            formulaBuilder.append(" | ");
          }
          formulaBuilder.append(String.format("(at(T, Train, %s) & gate(Train) = %s", previousNode.getLabel(), exitNode.getLabel()));
          if (previousNode.isEntrance()) {
            formulaBuilder.append(String.format(" & open(T, %s)", previousNode.getLabel()));
          }
          formulaBuilder.append(")");
          firstReached = true;
        }
        formulaBuilder.append(")))");
        builder.append(new FofBuilder(axiomName, "axiom", formulaBuilder.toString()).build());
        builder.append("\n");
      }
    }

    return builder.toString();
  }


  private String generateEntranceDeciding() {
    StringBuilder builder = new StringBuilder();
    List<StationNode> entrances = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());
    for (int i = 0; i < entrances.size(); i++) {
      int j;
      //getting the next node. if current is last, next is first
      if (i == entrances.size() - 1) {
        j = 0;
      } else {
        j = i + 1;
      }

      StationNode currentNode = entrances.get(i);
      StationNode nextNode = entrances.get(j);

      String currentNodeLabel = currentNode.getLabel();
      String nextNodeLabel = nextNode.getLabel();
      // MISTAKE!
      //String formulaBody = String.format("(![T]: ((entranceDeciding(T) = %1$s) <=> (entranceDeciding(succ(T)) = %1$s)))", currentNode.getLabel(), nextNode.getLabel());
      String formulaBody = String.format("(![T]: ((entranceDeciding(T) = %1$s) <=> (entranceDeciding(succ(T)) = %2$s)))", currentNodeLabel, nextNodeLabel);
      builder.append(new FofBuilder("entrance_deciding_" + i, "axiom", formulaBody).build());
      builder.append("\n");
    }

    //restrictions of entrance_deciding
    StringBuilder formulaBuilder = new StringBuilder();
    formulaBuilder.append("(![T]: (");
    boolean firstReached = false;
    for (StationNode entrance : entrances) {
      if (firstReached) {
        formulaBuilder.append(" | ");
      }
      firstReached = true;
      formulaBuilder.append(String.format("(entranceDeciding(T) = %s)", entrance.getLabel()));
    }
    formulaBuilder.append("))");
    builder.append(new FofBuilder("entrance_deciding_restr", "axiom", formulaBuilder.toString()).build());
    builder.append("\n");
    return builder.toString();
  }

  // for every path generate setting the switches target when the path is activated
  private String generatePathsConfigured() {
    StringBuilder builder = new StringBuilder();
    List<Path> paths = myStation.getPaths();
    for (int i = 0; i < paths.size(); i++) {
      Path path = paths.get(i);
      String axiomName = "path_" + Integer.toString(i) + "_configured";

      StringBuilder formulaBuilder = new StringBuilder();
      String formulaBody = String.format("(![T]: (isConfigured(T, %s) <=> (", path.toString());
      formulaBuilder.append(formulaBody);
      boolean firstSwitchReached = false;
      for (int j = 0; j < path.getNodes().size() - 1; j++) {
        StationNode currentNode = path.getNodes().get(j);
        if (currentNode.isSwitch()) {
          // means current node is switch
          if (firstSwitchReached) {
            // first switch was already printed, so add AND;
            formulaBuilder.append(" & ");
          }

          StationNode switchPointsTo = path.getNodes().get(j + 1);
          String format = String.format("(switch(T, %1$s) = %2$s)", currentNode.getLabel(), switchPointsTo.getLabel());
          formulaBuilder.append(format);
          firstSwitchReached = true;
        }
      }
      formulaBuilder.append(")))");

      builder.append(new FofBuilder(axiomName, "axiom", formulaBuilder.toString()).build());
      builder.append("\n");
    }

    return builder.toString();
  }

  private String generatePathIsSafe() {
    StringBuilder builder = new StringBuilder();
    List<Path> paths = myStation.getPaths();
    for (int i = 0; i < paths.size(); i++) {
      Path path = paths.get(i);
      String axiomName = "path_" + Integer.toString(i) + "_is_safe";

      StringBuilder formulaBuilder = new StringBuilder();
      formulaBuilder.append(String.format("(![T, Train]: (isSafe(T, %s)", path.toString()));

      Set<StationNode> dangerousNodes = new HashSet<>();
      for (Path otherPath : paths) {
        List<StationNode> dangerousNodesFromOtherPath = path.getNodesLeadingToMeFromOtherPath(otherPath);
        dangerousNodes.addAll(dangerousNodesFromOtherPath);
      }
      if (!dangerousNodes.isEmpty()) {
        formulaBuilder.append("<=> (");
      } else {
        formulaBuilder.append("))");
        builder.append(new FofBuilder(axiomName, "axiom", formulaBuilder.toString()).build());
        return builder.toString();
      }
      boolean firstNodeReached = false;

      for (StationNode dangerousNode : dangerousNodes) {
        // first node was already printed, so add AND;
        if (firstNodeReached) {
          formulaBuilder.append(" & ");
        }
        if (dangerousNode.isEntrance()) {
          // -- Added after deadline
          formulaBuilder.append(String.format("(~at(T, Train, %1$s) | ~(open(T,%1$s)))", dangerousNode.getLabel()));
          // --
          //continue;
        } else {
          formulaBuilder.append(String.format("(~at(T, Train, %s))", dangerousNode.getLabel()));
        }
        firstNodeReached = true;
      }
      formulaBuilder.append(")))");

      String formulaBody = formulaBuilder.toString();
      builder.append(new FofBuilder(axiomName, "axiom", formulaBody).build());
      builder.append("\n");
    }

    return builder.toString();
  }


  private String generatePathsToBeActivated() {
    StringBuilder builder = new StringBuilder();
    List<Path> paths = myStation.getPaths();
    for (int i = 0; i < paths.size(); i++) {
      Path path = paths.get(i);
      String axiomName = "path_" + Integer.toString(i) + "_is_ready_to_be_activated";
      List<StationNode> pathNodes = path.getNodes();
      StationNode entrance = pathNodes.get(0);
      StationNode exit = pathNodes.get(pathNodes.size() - 1);
      String formula = String.format(
          "![T]: ?[Train]: (toBeActivated(T, %1$s) <=> " +
              "((isSafe(T, %1$s)) & (at(T,Train,%2$s)) & " +
              "(entranceDeciding(T)=%2$s) & (gate(Train)=%3$s)))", path.toString(), entrance.getLabel(), exit.getLabel());
      builder.append(new FofBuilder(axiomName, "axiom", formula).build());
      builder.append("\n");
    }

    return builder.toString();

  }


  private String generateConfigurationRule() {

    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < myStation.getPaths().size(); i++) {
      Path path = myStation.getPaths().get(i);
      String formulaBody = String.format("(![T]: (((toBeActivated(T, %1$s)) | (isConfigured(T, %1$s) & ~isSafe(T, %1$s))) => isConfigured(succ(T), %1$s)))\n", path.toString());
      stringBuilder.append(new FofBuilder("config_path_" + i, "axiom", formulaBody).build());
    }
    return stringBuilder.toString();
  }

  private String generateOpenRule() {
    List<StationNode> entrances = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < entrances.size(); i++) {
      StringBuilder formulaBuilder = new StringBuilder();
      StationNode entrance = entrances.get(i);
      formulaBuilder.append(String.format("![T]: (open(succ(T),%s) <=> (", entrance.getLabel()));
      List<Path> pathsStartingWithEntrance = myStation.getPaths().stream().filter(path -> {
        StationNode pathEntrance = path.getNodes().get(0);
        return pathEntrance.equals(entrance);
      }).collect(Collectors.toList());
      boolean firstPathReached = false;
      for (Path path : pathsStartingWithEntrance) {
        if (firstPathReached) {
          formulaBuilder.append(" | ");
        }
        formulaBuilder.append(String.format("(toBeActivated(T, %s))", path.toString()));
        firstPathReached = true;
      }

      // -- Added after deadline
      formulaBuilder.append(String.format(" | (open(T, %1$s) & (?[Train]: (at(T, Train, %1$s) & ~go(T, Train))))", entrance.getLabel()));
      // --

      formulaBuilder.append("))");

      builder.append(new FofBuilder("open_" + i, "axiom", formulaBuilder.toString()).build());
      builder.append("\n");
    }
    return builder.toString();
  }

  private String generatePathsAreDifferent() {
    StringBuilder formulaBuilder = new StringBuilder();
    List<Path> paths = myStation.getPaths();
    if (paths.size() == 1) {
      return "";
    }


    boolean firstPairReached = false;
    for (int i = 0; i < paths.size(); i++) {
      Path firstPath = paths.get(i);
      for (int j = i + 1; j < paths.size(); j++) {
        if (firstPairReached) {
          formulaBuilder.append(" & ");
        }
        firstPairReached = true;
        Path secondPath = paths.get(j);
        formulaBuilder.append(String.format("(%1$s != %2$s)", firstPath.toString(), secondPath.toString()));
      }
    }
    return new FofBuilder("different_paths", "axiom", formulaBuilder.toString()).build();
  }
}
