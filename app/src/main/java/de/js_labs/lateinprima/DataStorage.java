package de.js_labs.lateinprima;

import java.util.ArrayList;


public class DataStorage {
    private static DataStorage ourInstance = new DataStorage();
    public final String LOG_TAG = "de.js_labs.log";
    public boolean firstStart;
    public boolean received_survey;

    public ArrayList<Vokablel> testVocBuffer;
    public Lektion[] lektions = new Lektion[50];

    public boolean removeAds;
    public boolean devMode;
    public boolean surveyRemoveAds;
    public boolean prima_surveys;
    public Long surveyTimeStamp;
    public int currentSurveyPrice;
    public boolean proveInput;
    public boolean ignoreCase;
    public Long dataTimeStamp;
    public int currentLektion;

    public boolean dataIsLeast = true;

    public static DataStorage getInstance() {
        return ourInstance;
    }

    private DataStorage() {
        firstStart = true;
        received_survey = false;
        surveyRemoveAds = false;
    }
}
