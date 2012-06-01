package com.alonsoruibal.chess;

public class Correlation {
	
	double N, sumx, sumy, sumsqx, sumsqy, sumxy;
	
	public Correlation() {
		init();
	}
	
	public void init() {
		N = 0;
		sumx = 0.0;
		sumy = 0.0;
		sumsqx = 0.0;
		sumsqy = 0.0;
		sumxy = 0.0;
	}
	
	public void add(double x, double y) {
		N++;
		sumx += x;
		sumy += y;
		sumsqx += x*x;
		sumsqy += y*y;
		sumxy += x*y;
	}
	
	public double get() {
		return (N * sumxy -  sumx*sumy) / Math.sqrt((N * sumsqx - sumx*sumx) * (N * sumsqy - sumy*sumy));	
	}
	
}
