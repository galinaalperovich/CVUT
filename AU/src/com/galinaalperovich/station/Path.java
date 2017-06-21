package com.galinaalperovich.station;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
  private final List<StationNode> myNodes;

  public Path(List<StationNode> nodes) {
    //copy the nodes
    myNodes = new ArrayList<>(nodes);
  }

  public List<StationNode> getNodes() {
    return myNodes;
  }


  //get all the nodes from other path from where this path os reachable
  public List<StationNode> getNodesLeadingToMeFromOtherPath(Path otherPath) {
    if (otherPath == this) {
      return myNodes.stream().filter(stationNode -> !stationNode.isEntrance() && !stationNode.isExit()).collect(Collectors.toList());
    }
    List<StationNode> result = new ArrayList<>();
    List<StationNode> subResult = new ArrayList<>();
    for (StationNode stationNode : otherPath.getNodes()) {
      //if (stationNode.isEntrance() || stationNode.isExit()) {
      // -- Added after deadline
      if (stationNode.isExit() || (stationNode.isEntrance() && stationNode.equals(myNodes.get(0)))) {
        // --
        continue;
      }
      subResult.add(stationNode);
      if (myNodes.contains(stationNode)) {
        result.addAll(subResult);
        subResult.clear();
      }
    }
    return result;
  }

  // string representation of the path containing labels of all nodes
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (StationNode node : myNodes) {
      builder.append(node.getLabel());
      if (!node.isExit()) {
        builder.append("_");
      }
    }
    return builder.toString();
  }
}
