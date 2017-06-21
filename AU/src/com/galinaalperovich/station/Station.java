package com.galinaalperovich.station;

import java.util.*;

public class Station {
    private final String myName;
    private Map<String, StationNode> myLabelToNodeMap = new HashMap<>();
    private List<StationNode> myNodes = new ArrayList<>();
    private List<Path> myPaths = new ArrayList<>();

    public Station(String name) {
        myName = name;
    }

    public String getName() {
        return myName;
    }

    public void addStationNode(StationNode node) {
        myLabelToNodeMap.put(node.getLabel(), node);
    }

    public StationNode getStationNode(String label) {
        return myLabelToNodeMap.get(label);
    }

    public List<StationNode> getStationNodes(){
        return myNodes;
    }

    public void done(){
        myNodes = new ArrayList<>(myLabelToNodeMap.values());
        calculatePaths();
    }

    private void calculatePaths() {
        getStationNodes().stream().filter(StationNode::isEntrance).forEach(node -> addPaths(node, new ArrayList<>()));
    }

    private void addPaths(StationNode start, List<StationNode> currentPathNodes) {
        currentPathNodes.add(start);
        if (start.getOutgoingNodes().isEmpty()) {
            myPaths.add(new Path(currentPathNodes));
        } else {
            for (StationNode next : start.getOutgoingNodes()) {
                addPaths(next, currentPathNodes);
            }
        }
        currentPathNodes.remove(start);
    }

    public List<Path> getPaths() {
        return myPaths;
    }

    public List<Path> getPathsByNodes(StationNode firstNode, StationNode secondNode){
        List<Path> result = new ArrayList<>();

        for (Path path : myPaths) {
            List<StationNode> nodes = path.getNodes();
            if (nodes.contains(firstNode)){
                int firstIndex = nodes.indexOf(firstNode);
                if (nodes.get(firstIndex + 1).equals(secondNode)){
                    result.add(path);
                }
            }
        }
        return result;

    }
}
