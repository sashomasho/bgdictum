package org.apelikecoder.bgdictum;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnPreferenceClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.setupTheme(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference(App.PreferenceKeys.preference_history).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        ((App) getApplication()).getRecentConnector().clearSearchHistory();
        return true;
    }

}
