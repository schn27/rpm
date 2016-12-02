/*
 * The MIT License
 *
 * Copyright 2016 Aleksandr Malikov <schn27@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package schn27.rpm;

import schn27.serial.Com;

/**
 *
 * @author Aleksandr Malikov <schn27@gmail.com>
 */
public class Generator implements Runnable {

	public Generator(Com port) {
		this.port = port;
		setFreq(100);
	}
	
	public void stop() {
		running = false;
		thread.interrupt();
		while (thread.isAlive()) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
			}
		}
	}
	
	public final void setFreq(float freq) {
		halfPeriodUs = (int)(500000 / freq);
	}
	
	@Override
	public void run() {
		running = true;
		thread = Thread.currentThread();
		port.open();
		
		while (!Thread.currentThread().isInterrupted() && running) {
			impulse(true, halfPeriodUs * 1000);
			impulse(impulseCounter >= 58, halfPeriodUs * 1000);
		
			if (++impulseCounter >= 60) {
				impulseCounter = 0;
			}
		}
		
		port.close();
	}
	
	private void impulse(boolean level, long durationNs) {
		port.setDTR(level);
		long end = System.nanoTime() + durationNs;
		while (System.nanoTime() < end) {}
	}
	
	private final Com port;
	private Thread thread;
	private volatile boolean running;
	private volatile int halfPeriodUs;
	private int impulseCounter;
}
