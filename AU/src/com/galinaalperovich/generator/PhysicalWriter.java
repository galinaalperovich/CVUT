package com.galinaalperovich.generator;


import com.galinaalperovich.station.Station;
import com.galinaalperovich.station.StationNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class PhysicalWriter {
  static final String FILE_NAME = "physical.p";
  private Station myStation;
  private static final String ONE_NODE_ONE_TIME = "![T,Train,N1,N2]: ((at(T, Train, N1) & at(T, Train, N2)) => (N1 = N2))";
  private static final String TRAIN_WILL_DEPART = "![T,Train,N]: ?[Next_T]: (at(T,Train,N) => ((less(T,Next_T) & go(Next_T,Train))))";

  // we don't use it, we use it after deadline
  private static final String TRAIN_APPEARED_SOMETIME = "![T,Train,N]: ?[Prev_T]: (at(T, Train, N) => ((less(Prev_T,T) & ~at(Prev_T,Train, N))))";

  private static final String TRAIN_DOESNT_ENTER_WHEN_ON_STATION = "![T,Train,N,OtherN]: (at(T, Train, N) => ~enter(T, Train, OtherN))";
  private static final String TRAIN_DOESNT_ENTER_OCCUPIED_NODE = "![T,Train,OtherTrain,N]: (at(T, Train, N) => ~enter(T, OtherTrain, N))";
  private static final String TWO_TRAIN_DONT_ENTER_SAME_NODE = "![T,Train1,Train2,N]: ((enter(T, Train1, N) & enter(T, Train2, N)) => (Train1 = Train2))";

  public PhysicalWriter(Station station) {
    myStation = station;
  }

  public void write() throws IOException {
    BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME));
    fileWriter.write(String.format("include ('%s').\n", AxiomsWriter.FILE_NAME));
    fileWriter.write("%Physical model of the station\n\n");

    fileWriter.write("%Movements of the trains: predicate at\n");

    fileWriter.write(generateAtAxioms());

    fileWriter.write("%Train can be only at one node at one time moment\n");
    fileWriter.write(generateOneNodeOneTime());

    fileWriter.write("%Every train departs at some time, so go(T,Train) will = true at some time\n");
    fileWriter.write(generateTrainWillDepart());

    //// -- Added after deadline
    fileWriter.write("%Every train wasn't  in the node all the time, it appeared in some time\n");
    fileWriter.write(generateAppearedSomeTime());

    //fileWriter.write("%Trains will appear at empty entrances sometime\n");
    //fileWriter.write(generateWillEnterSomeTime());
    // --

    fileWriter.write("%Train will not enter when it is already on the station\n");
    fileWriter.write(generateTrainDoesntEnterWhenOnStation());
//
    fileWriter.write("%Train will not enter occupied node\n");
    fileWriter.write(generateTrainDoesntEnterOccupiedNode());

    fileWriter.write("%Two trains will not enter same node same time\n");
    fileWriter.write(generateTwoTrainsDontEnterSameNode());
//

    // Exhaustive axiom for entrance nodes, removed after deadline
    //fileWriter.write("%There could be only one train at input\n");
    //fileWriter.write(generateOneTrainAtInput());

    // -- Added after deadline
    fileWriter.write("%When the train at exit node => next time it is not there\n");
    fileWriter.write(generateTrainOutOfExit());
    // --

    fileWriter.write("%Nodes are different\n");
    fileWriter.write(generateNodesAreDifferent());

    fileWriter.write("%If train is at station, then it is at one of the station nodes\n");
    fileWriter.write(generateAtOneOfTheNodes());

    fileWriter.write("%Train has gate\n");
    fileWriter.write(generateGates());
    fileWriter.close();
  }

  private String generateAtAxioms() {
    StringBuilder builder = new StringBuilder();

    builder.append("\n%Train is on the entrance node <=> it was appeared there at the previous moment or it is on the same node and (doesn't want to go or the signal is blocking it) \n");
    //add all at_entrances
    // for all entrance node we generate an axiom at(succ(T), Train, in)
    myStation.getStationNodes().stream().filter(StationNode::isEntrance).forEach(stationNode -> {
      String formulaName = "at_" + stationNode.getLabel();
      String formulaBody = generateEntranceFormula(stationNode);
      FofBuilder fofBuilder = new FofBuilder(formulaName, "axiom", formulaBody);
      builder.append(fofBuilder.build());
    });

    // for all NOT entrances we generate an axiom at(succ(T), Train, ...)
    builder.append("\n%Train is on the node <=> it was arrived from the previous node at the previous moment or it is on the same node and doesn't want to go\n");
    // add all at_(not entrances)
    myStation.getStationNodes().stream().filter(node -> !node.isEntrance()).forEach(stationNode -> {
      String formulaName = "at_" + stationNode.getLabel();
      String formulaBody = generateNonEntranceFormula(stationNode);
      FofBuilder fofBuilder = new FofBuilder(formulaName, "axiom", formulaBody);
      builder.append(fofBuilder.build());
    });

    //todo generate others;

    return builder.toString();
  }

  private String generateEntranceFormula(StationNode node) {
    String label = node.getLabel();

    return String.format("![T,Train]: (\n" +
        "\t\tat(succ(T), Train, %1$s) <=> ( \n" +
        "\t\t\t(enter(T, Train, %1$s)) | \n" +
        "\t\t\t(at(T, Train, %1$s) & (~go(T,Train) | ~open(T, %1$s)))\n" +
        "\t\t)\n" +
        "\t)", label);
  }

  private String generateNonEntranceFormula(StationNode node) {
    String label = node.getLabel();

    StringBuilder builder = new StringBuilder();
    builder.append("![T,Train]: (\n" +
        "\t\tat(succ(T), Train, " + label + ") <=> (\n");


    boolean firstNodeReached = false;
    for (StationNode incomingNode : node.getIncomingNodes()) {
      String incomingNodeLabel = incomingNode.getLabel();

      if (firstNodeReached) {
        // append OR in case current node is not first incoming node
        builder.append(" |\n");
      }

      if (incomingNode.isEntrance()) {
        builder.append(String.format("\t\t\t(at(T, Train, %1$s) & open(T, %1$s) & go(T, Train))", incomingNodeLabel));
      } else if (incomingNode.isSwitch()) {
        builder.append(String.format("\t\t\t(at(T, Train, %1$s) & (switch(T, %1$s) = %2$s) & go(T, Train))", incomingNodeLabel, label));
      } else {
        builder.append(String.format("\t\t\t(at(T, Train, %1$s) & go(T, Train))", incomingNodeLabel));
      }
      firstNodeReached = true;


    }
    if (!node.isExit()) {
      builder.append(String.format(" |\n\t\t\t(at(T, Train, %1$s) & ~go(T, Train))", node));
    }
    builder.append("\n\t\t)\n");
    builder.append("\t)");
    return builder.toString();
  }

  private String generateOneNodeOneTime() {
    return new FofBuilder("at_uniq", "axiom", ONE_NODE_ONE_TIME).build();
  }

  private String generateTrainWillDepart() {
    return new FofBuilder("train_will_depart", "axiom", TRAIN_WILL_DEPART).build();
  }

  private String generateAppearedSomeTime() {
    return new FofBuilder("train_appeared_sometime", "axiom", TRAIN_APPEARED_SOMETIME).build();
  }

  private String generateWillEnterSomeTime() {
    StringBuilder builder = new StringBuilder();
    List<StationNode> entrances = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());
    for (int i = 0; i < entrances.size(); i++) {
      String label = entrances.get(i).getLabel();
      String formulaBody = String.format("![T, Train]:(~at(T, Train, %1$s) => ?[Next_T, OtherTrain]:(less(T, Next_T) & enter(Next_T, OtherTrain, %1$s)))", label);
      String formula = new FofBuilder("train_will_enter" + i, "axiom", formulaBody).build();
      builder.append(formula);
      builder.append("\n");
    }

    return builder.toString();
  }


  private String generateTrainDoesntEnterWhenOnStation() {
    return new FofBuilder("at_nondup", "axiom", TRAIN_DOESNT_ENTER_WHEN_ON_STATION).build();
  }

  private String generateTrainDoesntEnterOccupiedNode() {
    return new FofBuilder("input_nonoccup", "axiom", TRAIN_DOESNT_ENTER_OCCUPIED_NODE).build();
  }

  private String generateTwoTrainsDontEnterSameNode() {
    return new FofBuilder("enter_uniq", "axiom", TWO_TRAIN_DONT_ENTER_SAME_NODE).build();
  }

  private String generateOneTrainAtInput() {
    List<StationNode> entranceNodes = myStation.getStationNodes().stream().filter(StationNode::isEntrance).collect(Collectors.toList());
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < entranceNodes.size(); i++) {
      StationNode entrance = entranceNodes.get(i);
      String formulaBody = String.format("![T,Train1,Train2]: ((at(T,Train1,%1$s) & at(T,Train2,%1$s)) => (Train1=Train2))", entrance.getLabel());
      builder.append(new FofBuilder("one_train_at_" + i, "axiom", formulaBody).build());
    }
    return builder.toString();
  }

  private String generateTrainOutOfExit() {
    List<StationNode> exitNodes = myStation.getStationNodes().stream().filter(StationNode::isExit).collect(Collectors.toList());
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < exitNodes.size(); i++) {
      StationNode exit = exitNodes.get(i);
      String formulaBody = String.format("![T,Train]: (at(T,Train,%1$s) => ~at(succ(T),Train,%1$s))", exit.getLabel());
      builder.append(new FofBuilder("train_out_of_exit_" + i, "axiom", formulaBody).build());
    }
    return builder.toString();
  }

  private String generateNodesAreDifferent() {
    StringBuilder formulaBuilder = new StringBuilder();
    List<StationNode> nodes = myStation.getStationNodes();

    boolean firstPairReached = false;
    for (int i = 0; i < nodes.size(); i++) {
      StationNode firstNode = nodes.get(i);
      for (int j = i + 1; j < nodes.size(); j++) {
        if (firstPairReached) {
          formulaBuilder.append(" & ");
        }
        firstPairReached = true;
        StationNode secondNode = nodes.get(j);
        formulaBuilder.append(String.format("(%1$s != %2$s)", firstNode.getLabel(), secondNode.getLabel()));
      }
    }
    String formulaBody = formulaBuilder.toString();
    return new FofBuilder("different_nodes", "axiom", formulaBody).build();
  }

  private String generateAtOneOfTheNodes() {
    StringBuilder formulaBuilder = new StringBuilder();
    formulaBuilder.append("![T, Train, N]: (at(T, Train, N) => (");
    boolean firstNodeReached = false;
    for (StationNode node : myStation.getStationNodes()) {
      if (firstNodeReached) {
        formulaBuilder.append(" | ");
      }
      firstNodeReached = true;
      formulaBuilder.append(String.format("(N = %s)", node.getLabel()));
    }
    formulaBuilder.append("))");

    String formulaBody = formulaBuilder.toString();
    return new FofBuilder("at_restr", "axiom", formulaBody).build();
  }


  private String generateGates() {
    StringBuilder formulaBuilder = new StringBuilder();
    formulaBuilder.append("![Train]: (");
    boolean firstGateReached = false;
    for (StationNode node : myStation.getStationNodes()) {
      if (!node.isExit()) {
        continue;
      }
      if (firstGateReached) {
        formulaBuilder.append(" | ");
      }
      firstGateReached = true;
      formulaBuilder.append(String.format("(gate(Train) = %s)", node.getLabel()));
    }

    formulaBuilder.append(")");

    String formulaBody = formulaBuilder.toString();
    return new FofBuilder("gates_restr", "axiom", formulaBody).build();
  }


}
