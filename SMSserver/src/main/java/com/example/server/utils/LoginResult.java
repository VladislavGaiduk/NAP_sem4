package com.example.server.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LoginResult {
    private final boolean success;
    private final String sourceTable;
    private final String data;


}