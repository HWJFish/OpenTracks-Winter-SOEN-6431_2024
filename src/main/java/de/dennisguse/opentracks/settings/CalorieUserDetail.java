package de.dennisguse.opentracks.settings;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import de.dennisguse.opentracks.data.models.ActivityType;
import de.dennisguse.opentracks.data.models.HeartRate;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.stats.TrackStatistics;

public class CalorieUserDetail {
    private int height;
    private int weight;
    private int age;
    private String sport;
    private int heartRate;

    Track track;
    public String getActivityType(){
        return track.getActivityType().getId();
    }

    TrackStatistics trackStatistics;

    public Duration getMovingTime(){
        return trackStatistics.getMovingTime();
    }

    public float getBPM(){
        return trackStatistics.getAverageHeartRate().getBPM();
    }

    // Constructor to initialize with dummy data
    public CalorieUserDetail() {
        this.height = 170;
        this.weight = 70;
        this.age = 30;
        this.sport = "Running";
        this.heartRate = 75;
    }

    // Getters and setters for height
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // Getters and setters for weight
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    // Getters and setters for age
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // Getters and setters for sport
    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    // Getters and setters for heart rate
    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    // Method to get data from GUI
    public void getDataFromGUI(Map<String, Object> guiData) {
        setHeight((int) guiData.getOrDefault("height", height));
        setWeight((int) guiData.getOrDefault("weight", weight));
        setAge((int) guiData.getOrDefault("age", age));
        setSport((String) guiData.getOrDefault("sport", sport));
        setHeartRate((int) guiData.getOrDefault("heartRate", heartRate));
    }
}
