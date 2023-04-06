package com.digdes.school.enums;

public enum Columns {
    ID("id"),
    LASTNAME("lastname"),
    AGE("age"),
    COST("cost"),
    ACTIVE("active");

    private String str;

    Columns(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public static Columns of(String s){
        for (Columns oprname : Columns.values()) {
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
