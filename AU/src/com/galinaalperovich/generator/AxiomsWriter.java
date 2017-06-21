package com.galinaalperovich.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AxiomsWriter {
  static final String FILE_NAME = "axioms.p";
  private static final String ANTISYMMETRY_FORMULA = "![X,Y]: ((less(X,Y) & less(Y,X)) => (X = Y))";
  private static final String TRANSITIVITY_FORMULA = "![X,Y,Z]: ((less(X,Y) & less(Y, Z)) => less(X, Z))";
  private static final String TOTALITY_FORMULA = "![X,Y]: (less(X,Y) | less(Y,X))";
  private static final String SUCC_FORMULA = "![X]: ((less(X,succ(X))) & (![Y]: (less(Y,X) | less(succ(X), Y))))";
  private static final String PRED_FORMULA = "![X]: (((pred(succ(X)) = X) & (succ(pred(X)) = X)))";
  //private static final String X_NOT_SUCC_X = "![X]: (X != succ(X))";

  public void write() throws IOException {
    BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(FILE_NAME));
    fileWriter.write("%Order: less; functions: succ and pred\n");
    fileWriter.write(generateAntisymmetry());
    fileWriter.write(generateTransitivity());
    fileWriter.write(generateTotality());
    fileWriter.write(generateSucc());
    fileWriter.write(generatePred());
    //fileWriter.write(generateXNotSuccX());

    fileWriter.close();
  }

  private String generateAntisymmetry() {
    return new FofBuilder("antisymmetry", "axiom", ANTISYMMETRY_FORMULA).build();
  }

  private String generateTransitivity() {
    return new FofBuilder("transitivity", "axiom", TRANSITIVITY_FORMULA).build();
  }

  private String generateTotality() {
    return new FofBuilder("totality", "axiom", TOTALITY_FORMULA).build();
  }

  private String generateSucc() {
    return new FofBuilder("succ", "axiom", SUCC_FORMULA).build();
  }

  private String generatePred() {
    return new FofBuilder("pred", "axiom", PRED_FORMULA).build();
  }

  //private String generateXNotSuccX() {
  //  return new FofBuilder("x_not_succ_x", "axiom", X_NOT_SUCC_X).build();
  //}



}
