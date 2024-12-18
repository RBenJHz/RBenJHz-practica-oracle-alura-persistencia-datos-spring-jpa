package com.fulldev.spectator.principal;

import java.util.Arrays;
import java.util.List;

public class EjemploStreams {
    public  void muestraEjemplo(){
        List<String> nombres = Arrays.asList("Ruben", "Dario", "Yaricela", "Patricia");
        nombres.stream()
                .sorted()
                .limit(4)
                .filter(n -> n.startsWith("R"))
                .map(n -> n.toUpperCase())
                .forEach(System.out::println);
    }
}
