package com.example.trakid;

public class ChildObject {

    String childName;
    String paircode;

    public ChildObject() {
    }

    public ChildObject(String childName, String paircode) {
        this.childName = childName;
        this.paircode = paircode;
    }

    public String getChildName() {
        return childName;
    }

    public String getPaircode() {
        return paircode;
    }
}
