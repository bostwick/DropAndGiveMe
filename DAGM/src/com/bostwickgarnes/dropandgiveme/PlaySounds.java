package com.bostwickgarnes.dropandgiveme;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class PlaySounds {
	private static final String TAG = "BackgroundMusic";
	
	private MediaPlayer m_player = new MediaPlayer();
	
	private Context m_context;

	public PlaySounds(Context context) {
		m_context = context;
		
	}

	
	public int getPosition() {
		return 0;	// don't care
	}
	
	
	public void play(int resourceID) {
		AssetFileDescriptor afd = m_context.getResources().openRawResourceFd(resourceID);

	    try
	    {   
	        m_player.reset();
	        m_player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
	        m_player.prepare();
	        m_player.start();
	        afd.close();
	    }
	    catch (IllegalArgumentException e)
	    {
	        Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
	    }
	    catch (IllegalStateException e)
	    {
	        Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
	    }
	    catch (IOException e)
	    {
	        Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
	    }

	}

	
	public void seekTo(int position) {
		// Do nothing
	}

	public void stop() {
		if ( m_player != null ) {
			m_player.stop();
			m_player.release();
			m_player = null;
		}
	}

}
