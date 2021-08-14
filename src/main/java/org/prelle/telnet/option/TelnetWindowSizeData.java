package org.prelle.telnet.option;

public class TelnetWindowSizeData {
	private int x;
	private int y;
	
	public TelnetWindowSizeData(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return "WindowSize = "+x+"*"+y;
	}
	public int getX() {return x;}
	public int getY() {return y;}
}