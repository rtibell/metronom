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

public class MetronomTimer {
	 private static int NUMBER_OF_LEDS = 8;
	 private static int NUMBER_OF_BUTTONS = 4;
	
	Timer timer;

	// create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

	GpioPinDigitalOutput led[];
	GpioPinDigitalInput button[];
	
	public MetronomTimer(int bpm) {
            init();
            int delay = (60 * 1000) / bpm;
            timer = new Timer();
            timer.scheduleAtFixedRate(new MetronomTask(delay/8, true), 2000, delay);
	}
	
	private void init() {
            // provision gpio pin #0, #2, #3, #4 as an output pin and turn on
            led = new GpioPinDigitalOutput[NUMBER_OF_LEDS];
            led[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "1a", PinState.HIGH);
            led[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "2a", PinState.HIGH);
            led[2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "3a", PinState.HIGH);
            led[3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "4a", PinState.HIGH);
            led[4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "1b", PinState.HIGH);
            led[5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "2b", PinState.HIGH);
            led[6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "3b", PinState.HIGH);
            led[7] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, "4b", PinState.HIGH);
            
            button = new GpioPinDigitalInput[NUMBER_OF_BUTTONS];
	    button[0] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_12, "but-1");
	    button[1] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_13, "but-2");
	    button[2] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_14, "but-3");
	    button[3] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "but-4");

            // set shutdown state for this pin
            for (int idx = 0; idx < NUMBER_OF_LEDS; idx++)
                if (led[idx] != null)
                    led[idx].setShutdownOptions(true, PinState.LOW);
        }
	
	
	public class MetronomTask extends TimerTask {
		private int pct;
		private int idx;
		private boolean oneeight = false;

		public MetronomTask(int pct, boolean oe) {
			this.pct = pct;
			this.oneeight = oe;
			this.idx = 0;
			System.out.println("Constructor for MetronomTask.");
		}

		@Override
		public void run() {
			
			try {
				int i = ((idx % 2) == 0) ? (idx/2) : (idx/2)+4;
				System.out.println("On-" + idx + "                   " + System.currentTimeMillis());
				led[i].high();
				Thread.sleep(this.pct);
				System.out.println("Off-" + idx + " " + System.currentTimeMillis());
				led[i].low();
                                idx = (idx+1) % 8;
                                System.out.println("B1=" + button[0].isHigh() + " B2=" +  button[1].isHigh() + " B3=" +  button[2].isHigh() + " B4=" +  button[3].isHigh());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	public static void main(String[] args) {
		MetronomTimer tt = new MetronomTimer(2*120);
		
	}

}

