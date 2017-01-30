package com.robynsilber.weather_forecast;

public class Weather {

    // Model data

    private String day;
    private double temperatureHi;
    private double temperatureLo;
    private String description;
    private String iconUrl;

    public Weather(){

    }

    public Weather(String day, double temperatureHi, double temperatureLo, String description, String iconCode){
        this.day = day;
        this.temperatureHi = temperatureHi;
        this.temperatureLo = temperatureLo;
        this.description = description;
        this.iconUrl = "http://openweathermap.org/img/w/" + iconCode + ".png";;
    }

    public String getDay(){
        return day;
    }

    public void setDay(String day){
        this.day = day;
    }


    public double getTemperatureHi() {
        return temperatureHi;
    }

    public void setTemperatureHi(double temperatureHi) {
        this.temperatureHi = temperatureHi;
    }

    public double getTemperatureLo() {
        return temperatureLo;
    }

    public void setTemperatureLo(double temperatureLo) {
        this.temperatureLo = temperatureLo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconCode) {
        this.iconUrl = "http://openweathermap.org/img/w/" + iconCode + ".png";
    }

    public String toString(){
        return day + " | " + Double.toString(temperatureHi) + " / " + Double.toString(temperatureLo) + " : " + description;
    }
}
