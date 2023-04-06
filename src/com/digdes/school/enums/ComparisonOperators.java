package com.digdes.school.enums;

public enum ComparisonOperators {

    EQUAL("="),
    NOT_EQUAL("!="),
    LIKE("like"),
    I_LIKE("ilike"),
    GREATER_OR_EQUAL(">="),
    LESS_OR_EQUAL("<="),
    GREATER(">"),
    LESS("<");

    private String str;

    ComparisonOperators(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public static ComparisonOperators of(String s){
        for (ComparisonOperators oprname : ComparisonOperators.values()) {
            if (s.equals(oprname.toString())) {
                return oprname;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return str;
    }
}