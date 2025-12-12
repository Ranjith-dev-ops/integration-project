package com.example;

public class App {
    public String greet() {
        return "Hello World";
    }

    public static void main(String[] args) {
        System.out.println(new App().greet());
    }
}

