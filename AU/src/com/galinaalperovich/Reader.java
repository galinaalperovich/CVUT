package com.galinaalperovich;

import com.galinaalperovich.station.Station;
import com.galinaalperovich.station.StationNode;

import java.io.InputStream;
import java.util.Scanner;

public class Reader {

    public static final String DIGRAPH = "digraph";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String ARROW = "->";

    Station read(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);

        //read "digraph" word according to the format
        String firstWord = scanner.next();
        if (!firstWord.equals(DIGRAPH)) {
            System.out.println("Wrong format: the first word should be " + DIGRAPH);
            return null;
        }

        // read "name"
        String name = scanner.next();
        Station station = new Station(name);

        //read "{" word according to the format
        String openBrace = scanner.next();
        if (!openBrace.equals(OPEN_BRACE)) {
            System.out.println("Wrong format: the name should be followed by " + OPEN_BRACE);
            return null;
        }

        String next;
        // reading the graph
        while (true) {
            next = scanner.next();
            if (next.equals(CLOSE_BRACE)) {
                break;
            }

            StationNode source = getStationNode(station, next);
            next = scanner.next();
            if (!next.equals(ARROW)) {
                System.out.println("Wrong format: the label should be followed by " + OPEN_BRACE);
                return null;
            }

            next = scanner.next();

            //remove ";" from the end
            next = next.substring(0, next.length() - 1);

            StationNode target = getStationNode(station, next);

            // add to the list of outgoing and incoming nodes
            source.getOutgoingNodes().add(target);
            target.getIncomingNodes().add(source);
        }
        station.done();
        return station;
    }

    private StationNode getStationNode(Station station, String label) {
        StationNode stationNode = station.getStationNode(label);

        // if the node doesn't exist
        if (stationNode == null) {
            stationNode = new StationNode(label);
            station.addStationNode(stationNode);
        }

        return stationNode;
    }
}
