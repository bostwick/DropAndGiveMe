package com.bostwickgarnes.dropandgiveme;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Holds all constants related to the settings and helper methods to get and set settings in shared preferences.
 */
public class SettingsData {
	public final static String SHARED_PREFS_FILE = "DagmSettings";
	public final static String ENCOURAGEMENT_SETTING = "encouragingSounds";
	public final static String SOUND_EFFECTS_SETTING = "soundEffects";
	
	private Context m_context;
	
	private SharedPreferences m_settings;
	
	public SettingsData(Context context){
		m_context = context;
		m_settings = m_context.getSharedPreferences(SHARED_PREFS_FILE, 0);
	}
	
	/**
	 * Gets the setting with the given key. If it doesn't exist, it returns the default value.
	 * @param key The key of the setting in shared preferences
	 * @param defaultVal The default value to return if there is no stored value
	 * @return The value of the setting
	 */
	public boolean getSettingValue(String key, boolean defaultVal){
		return m_settings.getBoolean(key, defaultVal);
	}
	
	/**
	 * Stores the setting with the given key and value. 
	 * @param key The key of the setting to store
	 * @param value The value to store
	 */
	public void putSettingValue(String key, boolean value){
		SharedPreferences.Editor editor = m_settings.edit();
		editor.putBoolean(key, value);	
		editor.commit();
	}
}
