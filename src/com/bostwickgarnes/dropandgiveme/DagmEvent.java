package com.bostwickgarnes.dropandgiveme;

import java.util.Date;


public class DagmEvent {
	public static enum EventType {
		CHALLENGE, TRAINING
	};
	
	public final EventType type;
	public final Date time;
	public final int id;
	public final boolean isWin;
	public final int score;
	
	protected DagmEvent(EventType type, Date time, int id, boolean isWin, int score) {
		this.type = type;
		this.time = time;
		this.id = id;
		this.isWin = isWin;
		this.score = score;
	}
	
	public static DagmEvent challenge(int id, boolean isWin, int score) {
		return challenge(new Date(), id, isWin, score);
	}
	
	public static DagmEvent challenge(Date time, int id, boolean isWin, int score) {
		return new DagmEvent(EventType.CHALLENGE, time, id, isWin, score);
	}
	
	public static DagmEvent training(int id, boolean isWin, int score) {
		return training(new Date(), id, isWin, score);
	}
	
	public static DagmEvent training(Date time, int id, boolean isWin, int score) {
		return new DagmEvent(EventType.TRAINING, time, id, isWin, score);
	}
	
	public byte[] toOutputBytes() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getTypeAsString()).append(",")
			.append(Utils.formatDate(this.time)).append(",")
			.append(id).append(",")
			.append(isWin).append(",")
			.append(score).append("\n");
		
		return sb.toString().getBytes();
	}
	
	protected String getTypeAsString() {
		switch(this.type) {
		case CHALLENGE: return "challenge";
		case TRAINING: return "training";
		default: return "";
		}
	}
}
