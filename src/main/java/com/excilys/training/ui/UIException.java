package com.excilys.training.ui;

public class UIException extends Exception {

    /**
    * Exception à jeter quand l'utilisateur rentre un nombre invalide.
    */
    public UIException() {
        System.out.println("Ce n'est pas un entier positif");
    }
}