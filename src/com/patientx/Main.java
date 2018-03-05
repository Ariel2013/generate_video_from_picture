package com.patientx;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		
		try {
			FfmpegUtil.build("/Users/xushenglai/Documents/temp/temp", "马尔代夫", "2017年10月4日");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
