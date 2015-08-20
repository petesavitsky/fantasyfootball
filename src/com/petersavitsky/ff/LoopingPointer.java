package com.petersavitsky.ff;

public class LoopingPointer {

	private final int maxSize;
	private int currentValue = 0;
	
	public LoopingPointer(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void increment() {
		currentValue++;
		if (currentValue > maxSize) {
			currentValue = 0;
		}
	}
	
	public void incrementBy(int increment) {
		for (int i = 0; i < increment; i++) {
			increment();
		}
	}
	
	public void set(LoopingPointer pointer) {
		currentValue = pointer.value();
	}
	
	public int value() {
		return currentValue;
	}

	@Override
	public String toString() {
		return "LoopingPointer [maxSize=" + maxSize + ", currentValue=" + currentValue + "]";
	}

	
	
}
