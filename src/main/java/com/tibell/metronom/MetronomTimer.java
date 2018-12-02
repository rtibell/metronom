package com.tibell.metronom;

import java.util.Timer;
import java.util.TimerTask;

public class MetronomTimer {
	Timer timer;
	
	public MetronomTimer(int bpm) {
		int delay = (60 * 1000) / bpm;
		timer = new Timer();
		timer.scheduleAtFixedRate(new MetronomTask(delay/8), 2000, delay);
	}
	
	public class MetronomTask extends TimerTask {

		private int pct;

		public MetronomTask(int pct) {
			this.pct = pct;
		}

		@Override
		public void run() {
			
			try {
				System.out.println("On                   " + System.currentTimeMillis());
				Thread.sleep(this.pct);
				System.out.println("Off " + System.currentTimeMillis());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	public static void main(String[] args) {
		MetronomTimer tt = new MetronomTimer(120);
		
	}

}

