package com.sjsu.cmpe275.helloWorld;

public class GreeterImpl implements Greeter {
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getGreeting() {
		return "Hello World from " + name + "!";
	}

}
