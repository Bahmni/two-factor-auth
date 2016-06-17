package org.bahmni.auth.twofactor;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration
@RestController
class Hello {

    @RequestMapping("/")
    String greet() {
        return "Hello World!";
    }
}
