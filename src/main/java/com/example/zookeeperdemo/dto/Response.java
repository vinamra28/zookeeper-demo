package com.example.zookeeperdemo.dto;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Response {
    private String data;
    private Object error;
    private boolean success = false;
}
