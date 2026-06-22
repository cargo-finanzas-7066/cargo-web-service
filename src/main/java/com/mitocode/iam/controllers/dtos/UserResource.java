package com.mitocode.iam.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResource {
    private Integer id;
    private String name;
    private String email;
    private String role;
}
