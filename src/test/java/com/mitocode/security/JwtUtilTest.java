package com.mitocode.security;

import com.mitocode.iam.persistence.entities.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {
    @Test void signedTokenCarriesUserAndRejectsAnotherIssuer(){
        var user=new UserEntity();user.setId(42);user.setEmail("advisor@cargo.pe");user.setRole(Role.ADVISOR);
        var jwt=new JwtUtil("a-secure-test-key-with-at-least-32-characters",28_800_000,"cargo-api");
        String token=jwt.generate(user);
        assertThat(jwt.validate(token)).isTrue();assertThat(jwt.getUserId(token)).isEqualTo(42);
        var other=new JwtUtil("a-secure-test-key-with-at-least-32-characters",28_800_000,"other-api");
        assertThat(other.validate(token)).isFalse();
    }
    @Test void refusesWeakSecret(){
        assertThatThrownBy(()->new JwtUtil("weak",1000,"cargo-api")).isInstanceOf(IllegalStateException.class);
    }
}
