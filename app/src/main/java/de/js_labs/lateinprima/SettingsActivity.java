package de.js_labs.lateinprima;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private SharedPreferences.Editor sharedDataEditor;
    private SharedPreferences.Editor sharedInputEditor;
    private SharedPreferences sharedPreferencesData;
    private SharedPreferences sharedPreferencesInput;

    private ListPreference doProvePref;
    private CheckBoxPreference ignoreCasePref;

    private DataStorage ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_pref);

        ds = DataStorage.getInstance();
        if(ds.firstStart){
            finish();
            return;
        }

        sharedPreferencesData = this.getSharedPreferences(MainActivity.SHARED_PREF, 0);
        sharedDataEditor = sharedPreferencesData.edit();
        sharedPreferencesInput = PreferenceManager.getDefaultSharedPreferences(this);
        sharedInputEditor = sharedPreferencesInput.edit();

        doProvePref = (ListPreference) findPreference(MainActivity.PROVE_INPUT);
        doProvePref.setOnPreferenceChangeListener(this);
        ignoreCasePref = (CheckBoxPreference) findPreference(MainActivity.IGNORE_CASE);
        if(sharedPreferencesData.getBoolean(MainActivity.PROVE_INPUT, false)){
            doProvePref.setValue("2");
        }else{
            doProvePref.setValue("1");
            ignoreCasePref.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        System.out.println(newValue);
        if(newValue.equals("1")){
            ignoreCasePref.setEnabled(false);
        }else {
            ignoreCasePref.setEnabled(true);
        }
        return true;
    }


    @Override
    public void onStop(){
        super.onStop();
        System.out.println(doProvePref.getValue());
        if(doProvePref.getValue().equals("1")){
            sharedDataEditor.putBoolean(MainActivity.PROVE_INPUT, false);
            ds.proveInput = false;
        }else {
            sharedDataEditor.putBoolean(MainActivity.PROVE_INPUT, true);
            ds.proveInput = true;
        }
        sharedDataEditor.putBoolean(MainActivity.IGNORE_CASE, sharedPreferencesInput.getBoolean(MainActivity.IGNORE_CASE, true));
        ds.ignoreCase = sharedPreferencesInput.getBoolean(MainActivity.IGNORE_CASE, true);
        sharedDataEditor.commit();
    }
}