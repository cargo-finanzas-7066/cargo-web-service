package com.mitocode.iam;

import com.mitocode.exception.TooManyRequestsException;
import com.mitocode.iam.services.implementations.LoginAttemptService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class LoginAttemptServiceTest {
    @Test void blocksAfterFiveFailuresAndClearsAfterSuccess(){
        var service=new LoginAttemptService();for(int i=0;i<5;i++)service.failure("User@Cargo.pe");
        assertThatThrownBy(()->service.check("user@cargo.pe")).isInstanceOf(TooManyRequestsException.class);
        service.success("USER@cargo.pe");assertThatCode(()->service.check("user@cargo.pe")).doesNotThrowAnyException();
    }
}
