package com.galinaalperovich;

import com.galinaalperovich.generator.AdditionalConditionsBuilder;
import com.galinaalperovich.generator.AxiomsWriter;
import com.galinaalperovich.generator.ConjectureGenerator;
import com.galinaalperovich.generator.ControlSystemWriter;
import com.galinaalperovich.generator.PhysicalWriter;
import com.galinaalperovich.station.Station;

import java.io.FileInputStream;
import java.io.IOException;

public class TPTPGenerator {

  public static void main(String[] args) {
    try {
      //Station station = new Reader().read(System.in);
      //Station station = new Reader().read(new FileInputStream("input.txt"));
      Station station = new Reader().read(new FileInputStream("input2.txt"));
      if (station == null) {
        return;
      }
      new AxiomsWriter().write();
      new PhysicalWriter(station).write();
      new AdditionalConditionsBuilder().write();
      new ControlSystemWriter(station).write();
      new ConjectureGenerator(station).write();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
