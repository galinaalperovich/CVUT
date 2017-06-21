package com.galinaalperovich.generator;

import com.galinaalperovich.station.Station;
import com.galinaalperovich.station.StationNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConjectureGenerator {
  private Station myStation;

  public ConjectureGenerator(Station station) {
    myStation = station;
  }

  public void write() throws IOException {
    writeTrainOnSwitchingNode();
    writeTwoTrainsOnSameNode();
    writeOpenEntrance();
  }

  private void writeTrainOnSwitchingNode() throws IOException {
    List<StationNode> switches = myStation.getStationNodes().stream().filter(StationNode::isSwitch).collect(Collectors.toList());
    for (StationNode node : switches) {
      BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get("prove_trainOnSwitchingNode_" + node.getLabel()+".p"));
      fileWriter.write(String.format("include ('%s').\n", ControlSystemWriter.FILE_NAME));
      fileWriter.write(generateAppearAlways(true));

      StringBuilder builder = new StringBuilder();
      builder.append("%" + String.format("Train should not be on %s when it is switched\n", node.getLabel()));
      builder.append(new FofBuilder("not_critical_on_switch_" + node.getLabel(), "conjecture", String.format("(![T, Train]: ((at(T, Train, %1$s) & at(succ(T), Train, %1$s)) => (switch(T, %1$s) = switch(succ(T), %1$s))))", node.getLabel())));
      fileWriter.write(builder.toString());
      fileWriter.close();
    }
  }

  // -- Edited after deadline (misspelling Train1, Train1 -> Train1, Train2)
  private void writeTwoTrainsOnSameNode() throws IOException {
    List<StationNode> nodes = myStation.getStationNodes();
    for (StationNode node : nodes) {
      BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get("prove_trainsOnSameNode_" + node.getLabel() + ".p"));
      fileWriter.write(String.format("include ('%s').\n", ControlSystemWriter.FILE_NAME));
      fileWriter.write(generateAppearAlways(true));
      StringBuilder builder = new StringBuilder();
      builder.append("%" + String.format("Two train should not be on %s at the same time\n", node.getLabel()));
      String formulaBody = String.format("(![T, Train1, Train2]: (((Train1 != Train2) & at(T, Train1, %1$s)) => ~at(T, Train2, %1$s)))", node.getLabel());
      builder.append(new FofBuilder("not_critical_two_trains_on_node_" + node.getLabel(), "conjecture", formulaBody));
      fileWriter.write(builder.toString());
      fileWriter.close();
    }
  }
  // --

  private void writeOpenEntrance() throws IOException {
    List<StationNode> entrances = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());;
    for (StationNode node : entrances) {
      BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get("prove_signalOpens_" + node.getLabel() + ".p"));
      fileWriter.write(String.format("include ('%s').\n", ControlSystemWriter.FILE_NAME));
      fileWriter.write(generateAppearAlways(false));
      StringBuilder builder = new StringBuilder();
      builder.append("%" + String.format("Entrace %s should not be closed every time\n", node.getLabel()));
      builder.append(new FofBuilder("not_critical_entrance_closed_" + node.getLabel(), "conjecture", String.format(" (?[T]: (open(T,%s)))", node.getLabel())));
      fileWriter.write(builder.toString());
      fileWriter.close();
    }
  }

  private String generateAppearAlways(boolean exists) {
    List<StationNode> entrances = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());
    StringBuilder builder = new StringBuilder();
    builder.append("%Additional condition that trains always appear\n");
    for (StationNode entrance : entrances) {
      if (!exists){
        builder.append(new FofBuilder("appear_always", "axiom", String.format("(![T, Train]: (enter(T,Train,%s)))", entrance)).build());
      } else {
        builder.append(new FofBuilder("appear_always", "axiom", String.format("(![T]:?[Train]: (enter(T,Train,%s)))", entrance)).build());
      }
    }
    builder.append("\n");
    return builder.toString();
  }
}
