package com.galinaalperovich.station;

import java.util.*;

public class StationNode {
  private String myLabel;
  private List<StationNode> myOutgoingNodes = new ArrayList<>();
  private List<StationNode> myIncomingNodes = new ArrayList<>();

  public StationNode(String label) {
    myLabel = label;
  }

  public String getLabel() {
    return myLabel;
  }

  public List<StationNode> getOutgoingNodes() {
    return myOutgoingNodes;
  }

  public List<StationNode> getIncomingNodes() {
    return myIncomingNodes;
  }

  public boolean isSwitch() {
    //Every node with more than one outgoing edge is a switch.
    return myOutgoingNodes.size() > 1;
  }

  public boolean isEntrance() {
    return myIncomingNodes.size() == 0;
  }

  public boolean isExit() {
    return myOutgoingNodes.size() == 0;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StationNode that = (StationNode) o;

    return myLabel.equals(that.myLabel);
  }

  @Override
  public int hashCode() {
    return myLabel.hashCode();
  }

  @Override
  public String toString() {
    return getLabel();
  }
}
