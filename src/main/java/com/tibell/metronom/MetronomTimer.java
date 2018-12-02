package com.tibell.metronom;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class MetronomTimer {
	 private static int NUMBER_OF_LEDS = 8;
	
	Timer timer;

	// create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

	GpioPinDigitalOutput led[];
	
	public MetronomTimer(int bpm) {
            init();
            int delay = (60 * 1000) / bpm;
            timer = new Timer();
            timer.scheduleAtFixedRate(new MetronomTask(delay/8), 2000, delay);
	}
	
	private void init() {
            // provision gpio pin #0, #2, #3, #4 as an output pin and turn on
            led = new GpioPinDigitalOutput[NUMBER_OF_LEDS];
            led[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "1a", PinState.HIGH);
            led[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "2a", PinState.HIGH);
            led[2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "3a", PinState.HIGH);
            led[3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "4a", PinState.HIGH);

            // set shutdown state for this pin
            for (int idx = 0; idx < NUMBER_OF_LEDS; idx++)
                if (led[idx] != null)
                    led[idx].setShutdownOptions(true, PinState.LOW);
        }
	
	
	public class MetronomTask extends TimerTask {
		private int pct;
		private int idx;

		public MetronomTask(int pct) {
			this.pct = pct;
			this.idx = 0;
			System.out.println("Constructor for MetronomTask.");
		}

		@Override
		public void run() {
			
			try {
				System.out.println("On-" + idx + "                   " + System.currentTimeMillis());
				led[idx].high();
				Thread.sleep(this.pct);
				System.out.println("Off-" + idx + " " + System.currentTimeMillis());
				led[idx].low();
                                idx = (idx+1) % 4;
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

