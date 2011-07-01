package com.bostwickgarnes.dropandgiveme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;

import com.bostwickgarnes.dropandgiveme.R;

public class FriendSelection extends ListActivity {
	private static final String TAG = "FriendSelection";
	
	private static final String FB_APP_ID 	= "120467011372515";
    private static final String FB_KEY 		= "facebook-session";
	private static final String FB_TOKEN 	= "access_token";
	private static final String FB_EXPIRES 	= "expires_in";
	
	public static final String EXTRA_CHALLENGE_ID = "challengeID";
	
	private Facebook m_facebook;
	private AsyncFacebookRunner m_AsyncRunner;
	private List<Map<String, String>> m_idAndFriendList = new ArrayList<Map<String,String>>();
	private static final String[] s_fromBindings = {"id", "name"};
	private static final int[] s_toBindings = 
						{R.id.dagm_friendselect_item_id, 
						 R.id.dagm_friendselect_item_name};
	
	DialogListener m_facebookListener = new DialogListener() {
		@Override
		public void onCancel() {
			Log.d(TAG, "Facebook Login Cancelled");
		} 
 
		@Override
		public void onComplete(Bundle values) {
			Log.d(TAG, "Facebook Login Completed");
			Log.d(TAG, values.toString());
			saveFacebookAccess();
			loadFriends();
		}

		@Override
		public void onError(DialogError e) {
			Log.d(TAG, "Facebook Login Error: " + e.getLocalizedMessage());
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());
		}		
	};
	
	private class FriendsRequestListener implements RequestListener {
	    String friendData;

	    //Method runs when request is complete
	    public void onComplete(String response, Object state) {
	        Log.d(TAG, "FriendListRequestONComplete");
	        //Create a copy of the response so i can be read in the run() method.
	        friendData = response; 

	        //Create method to run on UI thread
	        FriendSelection.this.runOnUiThread(new Runnable() {
	            public void run() {
	                try {
	                    //Parse JSON Data
	                    JSONObject json;
	                    json = Util.parseJson(friendData);

	                    //Get the JSONArry from our response JSONObject
	                    JSONArray friendArray = json.getJSONArray("data");

	                    //Loop through our JSONArray
	                    int friendCount = 0;
	                    String fId, fNm;
	                    JSONObject friend;
	                    for (int i = 0;i<friendArray.length();i++){
	                        //Get a JSONObject from the JSONArray
	                        friend = friendArray.getJSONObject(i);
	                        //Extract the strings from the JSONObject
	                        fId = friend.getString("id");
	                        fNm = friend.getString("name");
	                        Map<String,String> id2name = new HashMap<String,String>();
	                        id2name.put("name", fNm);
	                        id2name.put("id", fId);
	                        //Set the values to our arrays
	                        m_idAndFriendList.add(id2name);
	                        friendCount ++;
	                        Log.d("TEST", "Friend Added: " + fNm);
	                    }
	                    
	                    sortFriendList(m_idAndFriendList);
	                    setListAdapter(getFriendListAdapter());

	                } catch (JSONException e) {
	                    Log.d("TAG", "JSON error: " + e.getLocalizedMessage());
	                } catch (FacebookError e) {
	                	Log.d("TAG", "Facebook error: " + e.getLocalizedMessage());
	                }
	            }
	        });
	    }

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.d("TAG", "Facebook error: " + e.getLocalizedMessage());
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.d("TAG", "File Not Found error: " + e.getLocalizedMessage());
			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.d("TAG", "IO error: " + e.getLocalizedMessage());
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Log.d("TAG", "MalformedURLException error: " + e.getLocalizedMessage());
			
		}
	}

	
	private OnItemClickListener onFriendClicked = new OnItemClickListener() {
		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Map<String, String> item = (Map<String,String>)getListAdapter().getItem(position);
				String friendName = item.get("name");
				String friendId = item.get("id");
				//Log.d(TAG, "Chosen friend is: " + item.get("name"));
				
				
				Intent confirm = new Intent(FriendSelection.this, FriendConfirm.class);
				confirm.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				confirm.putExtra(FriendConfirm.EXTRA_FRIEND_ID, friendId);
				confirm.putExtra(FriendConfirm.EXTRA_FRIEND_NAME, friendName);
				confirm.putExtra(FriendConfirm.EXTRA_CHALLENGE_ID, getChallengeId());
				
				startActivity(confirm);
			}
		};
		
	/**
	 * @return	The Challenge ID passed to this activity as an intent extra
	 */
	public int getChallengeId() {
		return getIntent().getExtras().getInt(EXTRA_CHALLENGE_ID);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dagm_friend_selection); 
		
		
		m_facebook = new Facebook(FB_APP_ID);
		m_AsyncRunner = new AsyncFacebookRunner(m_facebook);
		
		loginAndGetFriends();
		getListView().setOnItemClickListener(onFriendClicked);
      
      
	}
	
	protected ListAdapter getFriendListAdapter() {
		return new SimpleAdapter(this, 
				m_idAndFriendList,
				R.layout.dagm_friendselect_item,
				s_fromBindings, s_toBindings);
	}
	
	/**
	 * 
	 * @param listOfMaps the list of maps to the id/name key value pairs
	 * @return a sorted list of maps by the name of each friend
	 */
	@SuppressWarnings("unchecked")
	static List<Map<String,String>> sortFriendList(List<Map<String,String>> listOfMaps) {
		try{
	     Collections.sort(listOfMaps, new Comparator() {
			public int compare(Object o1, Object o2) {
	        	  Map<String,String> O1 = (Map<String,String>)o1;
	        	  Map<String,String> O2 = (Map<String,String>)o2;
	               return (O1.get("name").compareTo(O2.get("name")));
	          }
	     });
		}
		catch(Exception e){
			Log.d(TAG,"Error sorting friend list: " + e.getLocalizedMessage());
		}
	    return listOfMaps;
	} 
	
	/**
	 * Check if already logged in, if so load the friends. 
	 * If not logged in, log in and then load the friends.
	 */
	private void loginAndGetFriends(){
		Log.d(TAG, "Getting friend list");
		
		if ( restoreFacebookAccess() ) {
			Log.d(TAG, "Facebook Session is valid");
			loadFriends();
		} else {
			Log.d(TAG, "Facebook Session is not valid");
			String[] permissions = {"publish_stream"};
			
			m_facebook.authorize(this, permissions, m_facebookListener);
		}
	}
	
	
	
	private boolean restoreFacebookAccess() {
	    SharedPreferences savedSession = getSharedPreferences(FB_KEY, Context.MODE_PRIVATE);
	    m_facebook.setAccessToken(savedSession.getString(FB_TOKEN, null));
	    m_facebook.setAccessExpires(savedSession.getLong(FB_EXPIRES, 0));
	    return m_facebook.isSessionValid();
	}
	
    private boolean saveFacebookAccess() {
    	Log.d(TAG, "saving facebook details");
        Editor editor = getSharedPreferences(FB_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(FB_TOKEN, m_facebook.getAccessToken());
        editor.putLong(FB_EXPIRES, m_facebook.getAccessExpires());
        return editor.commit();
    }
	
   @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        m_facebook.authorizeCallback(requestCode, resultCode, data);
    }
   
   /**
    * Makes a request to the facebookAPI and load the friends into
    * the friend list of maps.
    */
   public void loadFriends(){
	    try{
	        Log.d(TAG, "Getting Friends!");
	        //Create Request with Friends Request Listener
	        m_AsyncRunner.request("me/friends", new FriendsRequestListener());
	    } catch (Exception e) {
	        Log.d(TAG, "Exception: " + e.getMessage());
	    }
   }

	
}
