package com.fulldev.spectator.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
