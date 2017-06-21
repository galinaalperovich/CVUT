package com.galinaalperovich.generator;

public class FofBuilder {
    private static final String HEADER = "fof(";
    private final String myName;
    private final String myRole;
    private final String myFormula;

    public FofBuilder(String myName, String myRole, String myFormula) {
        this.myName = myName;
        this.myRole = myRole;
        this.myFormula = myFormula;
    }

    String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER);
        builder.append(myName);
        builder.append(", ");
        builder.append(myRole);
        builder.append(", (\n\t");
        builder.append(myFormula);
        builder.append("\n)).\n\n");
        return builder.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
