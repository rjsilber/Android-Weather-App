package com.robynsilber.weather_forecast.model;

public class Weather {

    // Model data

    private String mDay;
    private double mTemperatureHi;
    private double mTemperatureLo;
    private String mDescription;
    private String mIconUrl;

    public Weather(){

    }

    public Weather(String day, double temperatureHi, double temperatureLo, String description, String iconCode){
        this.mDay = day;
        this.mTemperatureHi = temperatureHi;
        this.mTemperatureLo = temperatureLo;
        this.mDescription = description;
        this.mIconUrl = "http://openweathermap.org/img/w/" + iconCode + ".png";;
    }

    public String getDay(){
        return mDay;
    }

    public void setDay(String day){
        this.mDay = day;
    }


    public int getTemperatureHi() {
        return (int) Math.round(mTemperatureHi);
    }

    public void setTemperatureHi(double temperatureHi) {
        this.mTemperatureHi = temperatureHi;
    }

    public double getTemperatureLo() {
        return (int) Math.round(mTemperatureLo);
    }

    public void setTemperatureLo(double temperatureLo) {
        this.mTemperatureLo = temperatureLo;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconCode) {
        this.mIconUrl = "http://openweathermap.org/img/w/" + iconCode + ".png";
    }

    public String toString(){
        return mDay + " | " + Double.toString(mTemperatureHi) + " / " + Double.toString(mTemperatureLo) + " : " + mDescription;
    }
}
