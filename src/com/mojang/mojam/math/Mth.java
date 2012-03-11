package com.mojang.mojam.math;

public class Mth {
	static double PI = Math.PI;
	static double PI2 = Math.PI*2;

	static public int clamp(int value, int low, int high) {
		if (value < low)
			return low;
		return value > high ? high : value;
	}

	public static double normalizeAngle(double a, double center) {
		return a - PI2 * Math.floor((a + PI - center) / PI2);
	}
}
