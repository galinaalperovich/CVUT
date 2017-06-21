package com.galinaalperovich.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AdditionalConditionsBuilder {
  static final String FILE_NAME = "physical_with_additional_condition.p";

  private static final String GO_ALWAYS = "![X,T]: (go(X,T))";


  public void write() throws IOException {
    BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME));
    fileWriter.write(String.format("include ('%s').\n", PhysicalWriter.FILE_NAME));
    fileWriter.write("%Additional condition which say that a train moves from one node to another as soon as possible\n");
    fileWriter.write(generateGoAlways());
    fileWriter.close();
  }

  private String generateGoAlways() {
    return new FofBuilder("go_always", "axiom", GO_ALWAYS).build();
  }


}
