package com.example.skif.testingapp;

import java.util.UUID;

/**
 * Created by skif on 19.12.2014.
 */
public class PhoneInfo {
    public UUID Id = UUID.randomUUID();

    public String PhoneName = "my phone";

    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(PhoneName);
        return str.toString(); }
}
