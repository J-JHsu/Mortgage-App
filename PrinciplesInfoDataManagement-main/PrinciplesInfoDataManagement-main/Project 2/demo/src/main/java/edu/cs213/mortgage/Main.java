package edu.cs213.mortgage;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Mortgage Backed Securities System");
        MortgageApp app = new MortgageApp(); 
        app.run();  // Starts the main program loop
    }
}