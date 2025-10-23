package com.hakaton.tkp.model;

import lombok.Data;
import java.util.List;

@Data
public class UserProfile {
    private String name;
    private int age;
    private String city;
    private List<String> interests;
}