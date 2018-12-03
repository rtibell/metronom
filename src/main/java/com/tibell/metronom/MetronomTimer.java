package com.tibell.metronom;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class MetronomTimer {
	private static final int NUMBER_OF_NOTES = 4;
	private static final int NUMBER_OF_LEDS = 2*NUMBER_OF_NOTES;
	private static final int NUMBER_OF_BUTTONS = 4;
	private static final int BUTTON_STOP_INDEX = 0;
	private static final int BUTTON_START_INDEX = 2;
	private static final int BUTTON_FORWARD_INDEX = 1;
	private static final int BUTTON_BACKWARD_INDEX = 3;

	private Timer timer = null;
	private boolean oneeight = false; 
	private int bpm = 120;

	// create gpio controller
	final GpioController gpio = GpioFactory.getInstance();

	GpioPinDigitalOutput led[];
	GpioPinDigitalInput button[];

	public MetronomTimer(int bpm, boolean oe) {
		this.bpm = bpm;
		this.oneeight = oe;
		init();
	}

	
	public int getBpm() {
		return this.bpm;
	}


	public void setBpm(int bpm) {
		this.bpm = bpm;
	}


	public int getDelay() {
		return  (60 * 1000) / getBpm();
	}


	public void start() {
		if (timer != null) {
			stop();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(new MetronomTask(getDelay() / NUMBER_OF_NOTES), 2, getDelay());
		
	}
	
	public void stop() {
		if (timer != null) timer.cancel();
		timer = null;
	}

	public void shutdown() {
		if (timer != null) timer.cancel();
		 gpio.shutdown();
	}
	
	public void forward() {
		// ToDo temoprary test code, fix this.
		stop();
		setBpm(getBpm() + 10);
		if (timer != null) start();
	}
	
	public void backward() {
		// ToDo temoprary test code, fix this.
		stop();
		setBpm(getBpm() - 10);
		if (timer != null) start();
	}
	
	private void init() {
		// provision gpio pin #0, #2, #3, #4 as an output pin and turn on
		led = new GpioPinDigitalOutput[NUMBER_OF_LEDS];
		led[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "1a", PinState.LOW);
		led[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "2a", PinState.LOW);
		led[2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "3a", PinState.LOW);
		led[3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "4a", PinState.LOW);
		led[4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "1b", PinState.LOW);
		led[5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "2b", PinState.LOW);
		led[6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "3b", PinState.LOW);
		led[7] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, "4b", PinState.LOW);

		button = new GpioPinDigitalInput[NUMBER_OF_BUTTONS];
		for (int i = 0 ; i < NUMBER_OF_BUTTONS; i++)
			switch(i) {
			case BUTTON_STOP_INDEX:
				button[i] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_12, "Stop btn");
				button[i].setShutdownOptions(true);
				button[i].addListener(new GpioPinListenerDigital() {
		            @Override
		            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		                // display pin state on console
		                System.out.println(" --> BUTTON_STOP STATE CHANGE: " + event.getPin() + " = " + event.getState());
		                if (event.getState() == PinState.HIGH) stop();
		            }
		        });
				break;
			case BUTTON_START_INDEX:
				button[i] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_14, "Start btn");
				button[i].setShutdownOptions(true);
				button[i].addListener(new GpioPinListenerDigital() {
		            @Override
		            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		                // display pin state on console
		                System.out.println(" --> BUTTON_START STATE CHANGE: " + event.getPin() + " = " + event.getState());
		                if (event.getState() == PinState.HIGH) start();
		            }
		        });
				break;
			case BUTTON_FORWARD_INDEX:
				button[i] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_13, "Forward btn");
				button[i].setShutdownOptions(true);
				button[i].addListener(new GpioPinListenerDigital() {
		            @Override
		            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		                // display pin state on console
		                System.out.println(" --> BUTTON_FORWARD STATE CHANGE: " + event.getPin() + " = " + event.getState() + " / " + getBpm());
		                if (event.getState() == PinState.LOW) forward();
		            }
		        });
				break;
			case BUTTON_BACKWARD_INDEX:
				button[i] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "Backward btn");
				button[i].setShutdownOptions(true);
				button[i].addListener(new GpioPinListenerDigital() {
		            @Override
		            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		                // display pin state on console
		                System.out.println(" --> BUTTON_BACKWARD STATE CHANGE: " + event.getPin() + " = " + event.getState() + " / " + getBpm());
		                if (event.getState() == PinState.LOW) backward();
		               
		            }
		        });
				break;
			}


		// set shutdown state for this pin
		for (int idx = 0; idx < NUMBER_OF_LEDS; idx++)
			if (led[idx] != null)
				led[idx].setShutdownOptions(true, PinState.LOW);
	}

	public class MetronomTask extends TimerTask {
		private int blinkTime;
		private int idx;

		public MetronomTask(int blinkTime) {
			this.blinkTime = blinkTime;
			if (oneeight) {
				this.blinkTime = blinkTime/2;
			}
			this.idx = 0;
			System.out.println("Constructor for MetronomTask.");
		}

		@Override
		public void run() {

			try {
				int i = ((idx % 2) == 0) ? (idx / 2) : (idx / 2) + 4;
				//System.out.println("On-" + idx + "                   " + System.currentTimeMillis());
				led[i].high();
				Thread.sleep(this.blinkTime);
				//System.out.println("Off-" + idx + " " + System.currentTimeMillis());
				led[i].low();
				idx = (idx + 1) % 8;
				//System.out.println("B1=" + button[0].isHigh() + " B2=" + button[1].isHigh() + " B3="
				//		+ button[2].isHigh() + " B4=" + button[3].isHigh());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) throws InterruptedException {
		MetronomTimer tt = new MetronomTimer(120, true);
		System.out.println("Pre start");
		tt.start();
		System.out.println("Post start");
		Thread.sleep(30000);
		System.out.println("Pre stop");
		tt.stop();
		tt.shutdown();
		System.out.println("Post stop");
	}

}
