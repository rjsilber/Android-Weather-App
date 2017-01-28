package com.robynsilber.weather_forecast;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class WeatherData {

    private ArrayAdapter<String> mWeatherDataAdapter;

    public WeatherData(double latitude, double longitude){

        WeatherDataAsyncTask weatherDataAsyncTask = new WeatherDataAsyncTask();

    }

    // takes care of executing the AsyncTask
    private void updateWeather(){
        WeatherDataAsyncTask weatherDataAsyncTaskTask = new WeatherDataAsyncTask(); // creates new AsyncTask

        // TODO: read from shared preferences to get the default or saved settings

    }



    public class WeatherDataAsyncTask extends AsyncTask<String, Void, String[]>{
        /**
         * WeatherDataAsyncTask defines a class that extends AsyncTask for
         * downloading Weather data on a background thread.
         */


        // Tag for error logging in logcat
        private final String LOG_TAG = WeatherDataAsyncTask.class.getSimpleName();

        // The following method is must be implemented as WeatherDataAsyncTask is a subclass of AsyncTask
        @Override
        protected String[] doInBackground(String... params) {

            // QUERY PARAMS - define as constants
            // Separately define query params for better maintainability, flexibility, and readability of the code,
            // these params may need to be updated in the event that the OWM API changes in the future:

            // BASE_URL for lat, lon query:
            // http://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}

            // BASE_URL for zip code query:
            // http://api.openweathermap.org/data/2.5/forecast?id={city ID}

            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String COUNT_PARAM = "cnt"; // number of days param
            final String APPID_PARAM = "APPID";

            // TWO SCENARIOS: String param is empty or not empty.
            // Ensure that it is not empty, and if so, return (i.e., no location)

            // Initially uses zip code for location
            if(params.length == 0){ // checks if params is empty
                return null; // no zip code, no location means no data to retrieve
            }

            // Declare the String variable to store the JSON data retrieved from the query:
            String jsonData = null;

            // This line of code is only reachable if params is not empty
            // Next step: retrieve weather data from OpenWeatherMap API within try/catch/finally block
            // Note that HttpURLConnection and BufferedReader variables will need to be closed outside of
            //  the catch block. Declare variables for HttpURLConnection, BufferedReader outside (before) catch block
            // Initialize to null.
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            // In the event of an error, initializing these to null will smooth the disconnect/close process
            // for these instantiations (in the event that they end up not getting properly initialized and/or
            // used within the try block below.

            // values for the QUERY PARAMS
            String format = "json";
            String units = "imperial"; // value for UNITS_PARAM to request weather forecasts in Fahrenheit
            // To retrieve the weather in Celsius units, set units to "metric"
            int howManyDays = 5; // number of days to retrieve weather forecast data for

            try{
                // Build URL for a query to OWM API by first appending a Uri with the PARAMS
                Uri uri = Uri
                        .parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0]) // reads the 0th pos in the params array since we only passed in one string for the params array, the postal code
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(COUNT_PARAM, Integer.toString(howManyDays)) // howManyDays is an int, to convert to string
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OWM_API_KEY) // build config gets the API key String from gradle.properties
                        .build();

                // Declare the URL, using the uri String as arg
                URL url = new URL(uri.toString()); // this line requires an IOException as the catch block param

                // Begin query to OWM server by opening httpURLConnection
                httpURLConnection = (HttpURLConnection) url.openConnection(); // assignment statement; cast URL to HttpURLConnection

                // Set the request method of the httpURLConnection to "GET"
                httpURLConnection.setRequestMethod("GET");

                // Connect to server
                httpURLConnection.connect();

                // Next step: Retrieve the weather data
                // Proceed by getting the input stream returned from the query
                InputStream inputStream = httpURLConnection.getInputStream();

                // TWO SCENARIOS: input stream is empty or not empty
                // In the event that it is empty, no data was returned from the query (i.e., nothing to parse)
                if(inputStream == null){
                    return null; // no data retrieved from server
                }

                // Next step: Parse the weather data retrieved from the query code above
                // Components needed for parsing logic:
                // BufferedReader (declared before catch block), InputStreamReader and StringBuffer (both declared below).
                // Declare and initialize InputStreamReader by passing in inputStream (contains the retrieved weather data) as input arg.
                // Initialize BufferedReader using constructor, and constructed InputStreamReader
                // Declare and initialize StringBuffer
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                // The following two components will be used within the parsing logic loop
                bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();

                // Parse and read the data line by line using the bufferedReader
                String line;
                final String NEW_LINE = "\n";

                while((line=bufferedReader.readLine()) != null){
                    // append stringBuffer by adding new line escape char.
                    // This makes the data more readable, while simplifying for testing and debugging purposes
                    stringBuffer.append(line + NEW_LINE);
                } // all the data has been initially parsed and appended

                // TWO SCENARIOS: stringBuffer is empty ot not empty
                // In the event that it is empty, it contains no data, and thus nothing for further
                // parsing and/or converting and no content available to display to user in the UI

                if(stringBuffer.length() == 0){ // nothing to parse
                    return null;
                }

                // If this line of code has been reached, we have successfully retrieved weather
                // data from the server using the Open Weather Map API with a unique key.
                // Convert the data in the stringBugger to a String, which is the value for
                // the jsonData variable, declared before the try block and initially set to null
                jsonData = stringBuffer.toString();

            }catch(IOException e){ // log error
                Log.e(LOG_TAG, "Error: ", e);
                return null;
            }finally{
                // if this line of code has been reached, then the httpURLConnection and
                // the bufferedReader have not yet been diconnected/closed.
                // Next step: disconnect the httpURLConnection if still open
                // then, close the buffered reader within a try/catch block
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }

                if(bufferedReader != null){
                    try{
                        bufferedReader.close();
                    } catch(final IOException e){
                        Log.e(LOG_TAG, "Error: BufferedReader failed to close: ", e);
                    }
                }
            }

            try{
                String[] formattedForecast = formatJsonArray(jsonData, howManyDays);
                return formattedForecast;

            } catch (JSONException e){
                Log.e(LOG_TAG, "Error: failed to format JSON data: ", e);
            }

            // failed
            return null;

        }


        // The following method runs on the main UI thread
        @Override
        protected void onPostExecute(String[] data){
            // method receives String array of forecast data, which was the
            // value returned from the above doInBackground() method

            // TWO SCENARIOS: data is empty or not empty
            // if not empty, use the Adapter to clear of all previous forecast entries
            if(data != null){
                mWeatherDataAdapter.clear();

                for(String dailyForecastData : data){ // adds each new forecast entry to the forecastAdapter
                    mWeatherDataAdapter.add(dailyForecastData); // triggers the updating of the ListView list items
                }
            }
        }


        private String[] formatJsonArray(String jsonData, int howManyDays) throws JSONException {

            // Declare and initialize String[] with total elements = number of days of forecast data
            String[] formattedData = new String[howManyDays]; // will be returned from this method


            // TODO: shared preferences for units

            // Use deprecated Time class to build a Time object, set time to now
            // Note: the forecast data will be ordered chronologically

            Time dateObj = new Time();
            dateObj.setToNow(); // sets time of time object to the current date/time
            int dayOneForecast = Time.getJulianDay(System.currentTimeMillis(), dateObj.gmtoff);
            dateObj = new Time();

            // Tags for JSON data extraction
            final String LIST = "list";
            final String WEATHER = "weather";
            final String MAIN = "main";
            final String TEMP = "temp";
            final String MAX = "max";
            final String MIN = "min";


            // Construct a JSONObject from the data
            JSONObject jsonObject = new JSONObject(jsonData);
            // Construct a JSONArray by extracting the "list" element
            JSONArray jsonArray = jsonObject.getJSONArray(LIST);

            // Each i-th position of the jsonArray represents a day's worth of weather data
            for(int i=0; i<jsonArray.length(); i++){

                JSONObject jsonDayObj = jsonArray.getJSONObject(i); // one day of weather data
                JSONObject jsonWeatherObj = jsonDayObj.getJSONArray(WEATHER).getJSONObject(0);
                JSONObject jsonTemperatureObj = jsonDayObj.getJSONObject(TEMP);

                long date = dateObj.setJulianDay(dayOneForecast + i);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd");

                String forecast = simpleDateFormat.format(date) + " - "
                        + jsonWeatherObj.getString(MAIN) + " - "
                        + Math.round(jsonTemperatureObj.getDouble(MAX)) + " - "
                        + Math.round(jsonTemperatureObj.getDouble(MIN));

                formattedData[i] = forecast;

            }

            return formattedData;
        }

    }
}
