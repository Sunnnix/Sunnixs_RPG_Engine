package de.sunnix.sunlundra;

public class Main {

    public static void main(String[] args) {
        System.out.println(BuildData.getData("name") + " Version: " + BuildData.getData("version"));
    }

}
