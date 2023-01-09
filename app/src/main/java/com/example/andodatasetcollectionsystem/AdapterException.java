package com.example.andodatasetcollectionsystem;

import java.io.IOException;

public class AdapterException{
    static class NoAdapterException extends Exception{
        public NoAdapterException(){
            super("There is no adapter.");
        }
    }
    static class NotFoundException extends Exception{
        public NotFoundException(){
            super("Not found OBD");
        }
    }

}

