package com.bostwickgarnes.dropandgiveme;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ChallengeXmlHandler extends DefaultHandler {
	private static final String TAG = "ChallengeXmlHandler";
	
	private static final String DOC_ELEM = "DropAndGiveMe";
	private static final String CHALLENGE_ELEM = "Challenge";
	private static final String TITLE_ELEM = "Title";
	private static final String DESC_ELEM = "Description";
	private static final String ROUND_ELEM = "Round";
	private static final String EXERCISE_ELEM = "Exercise";
	
	private static final String ID_ATTR = "id";
	private static final String LEVEL_ATTR = "level";
	private static final String TIMEDIR_ATTR = "timeDir";
	private static final String TRAINING_ATTR = "training";
	
	private static final String TYPE_ATTR = "type";
	private static final String REPS_ATTR = "reps";
	private static final String TIME_ATTR = "time";
	private static final String SPEED_ATTR = "speed";
	private static final String CONSISTENT_ATTR = "consistent";
	
	private Stack<String> m_elemStack = new Stack<String>();
	private List<Challenge> m_challenges = new ArrayList<Challenge>();
	
	// The challenge being built by a <Challenge> node
	private Challenge m_currentChallenge = null;
	
	// The round being built by a <Round> node
	private Round m_currentRound = null;
	
	private int m_minLevel = 0;
	private int m_maxLevel = 0;
	
	public List<Challenge> getChallenges() {
		return m_challenges;
	}
	
	public int getMinLevel() {
		return m_minLevel;
	}
	
	public int getMaxLevel() {
		return m_maxLevel;
	}
	
	@Override
	public void startDocument() throws SAXException 
	{
		super.startDocument();
		Log.v(TAG, "Parsing challenges");
		
		m_elemStack.push(DOC_ELEM);
	}
	
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException 
	{
		super.startElement(uri, localName, qName, attributes);
	
		//Log.v(TAG, "Found element " + localName);
		
		if ( localName.equals(DOC_ELEM) ) 
		{
			m_elemStack.push(DOC_ELEM);
		}
		else if ( localName.equals(CHALLENGE_ELEM) )
		{
			m_elemStack.push(CHALLENGE_ELEM);
			m_currentChallenge = new Challenge();
			handleChallengeAttributes(attributes);
		}
		else if ( localName.equals(TITLE_ELEM) ) 
		{
			m_elemStack.push(TITLE_ELEM);
		}
		else if ( localName.equals(DESC_ELEM) )
		{
			m_elemStack.push(DESC_ELEM);
		}
		else if ( localName.equals(ROUND_ELEM) )
		{
			m_elemStack.push(ROUND_ELEM);
			m_currentRound = new Round();
		}
		else if ( localName.equals(EXERCISE_ELEM) )
		{
			m_elemStack.push(EXERCISE_ELEM);
			handleExercise(attributes);
		}
		else
		{
			Log.e(TAG, "Invalid element found: " + localName);
		}
	}
	
	public void handleChallengeAttributes(Attributes attributes) {
		String idStr = attributes.getValue(ID_ATTR);
		String levelStr = attributes.getValue(LEVEL_ATTR);
		String timeDirStr = attributes.getValue(TIMEDIR_ATTR);
		String isTrainingStr = attributes.getValue(TRAINING_ATTR);
		
		boolean isTraining = 
			(isTrainingStr == null) ? false : isTrainingStr.equalsIgnoreCase("true");
		
		boolean timeCountsUp = 	
			(timeDirStr == null) ? true : timeDirStr.equalsIgnoreCase("up");
		
		int level = Integer.parseInt(levelStr);

		if ( m_minLevel == 0  || level < m_minLevel ) {
			m_minLevel = level;
		}
		
		if ( m_maxLevel == 0 || level > m_maxLevel ) {
			m_maxLevel = level;
		}
		
		// Log.v(TAG, "id = " + idStr + ", level = " + levelStr + ", timeCountsUp = " + timeCountsUp);
		
		m_currentChallenge.setId(Integer.parseInt(idStr));
		m_currentChallenge.setLevel(level);
		m_currentChallenge.timeCountsUp(timeCountsUp);
		m_currentChallenge.isTraining(isTraining);
	}
	
	
	public void handleExercise(Attributes attributes) {
		String typeStr = attributes.getValue(TYPE_ATTR);
		String repStr = attributes.getValue(REPS_ATTR);
		String timeStr = attributes.getValue(TIME_ATTR);
		String consistentStr = attributes.getValue(CONSISTENT_ATTR);
		String speedStr = attributes.getValue(SPEED_ATTR);
				
		Exercise exercise = Exercise.ofType(typeStr);
		
		if ( repStr != null ) {
			exercise.setReps(Integer.parseInt(repStr));
		}
		
		if ( timeStr != null ) {
			exercise.setTime(Integer.parseInt(timeStr));
		}
		
		if ( consistentStr != null ) {
			exercise.isConsistentSpeed(Boolean.parseBoolean(consistentStr));
		}
		
		if ( speedStr != null ) {
			exercise.setSpeed(Integer.parseInt(speedStr));
		}
		
		exercise.decideDirections();
		m_currentRound.addExercise(exercise);
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException 
	{
		super.characters(ch, start, length);
		String content = new String(ch).substring(start, start + length);

		if ( m_elemStack.peek().equals(TITLE_ELEM) )
		{
			m_currentChallenge.setTitle(content);
		}
		else if ( m_elemStack.peek().equals(DESC_ELEM) )
		{
			m_currentChallenge.setDescription(content);	
		}
		else
		{
			// Do nothing
		}
	}
	
	
	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException 
	{
		super.endElement(uri, localName, qName);
		
		if ( m_elemStack.pop().equals(localName) )
		{
			if ( localName.equals(CHALLENGE_ELEM) ) 
			{
				if ( m_currentChallenge != null && m_currentChallenge.isValid() ) {
					m_challenges.add(m_currentChallenge);
					m_currentChallenge = null;
				} else {
					Log.e(TAG, "Incomplete challenge. Skipping: " + m_currentChallenge);
				}
			}
			else if ( localName.equals(ROUND_ELEM) ) 
			{
				if ( m_currentRound != null && m_currentRound.isValid() ) {
					m_currentChallenge.addRound(m_currentRound);
					m_currentRound = null;
				} else {
					Log.e(TAG, "Incomplete round. Skipping: " + m_currentRound);
				}
			}
		}
		else
		{
			Log.e(TAG, "Found unexpected end element: " + localName);
		}
	}
	
	
	@Override
	public void endDocument() throws SAXException 
	{
		super.endDocument();
		
		if ( m_elemStack.pop().equals(DOC_ELEM) ) {
			Log.v(TAG, "Finished parsing challenges");
		} else {
			Log.e(TAG, "ERROR: Challenges xml is incomplete!");
		}
	}
}
