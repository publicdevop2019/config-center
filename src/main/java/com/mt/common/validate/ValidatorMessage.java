package com.mt.common.validate;

import lombok.Data;

@Data
public class ValidatorMessage {
    private String type;
    private String message;
    private String key;
}
