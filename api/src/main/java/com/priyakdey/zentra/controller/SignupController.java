package com.priyakdey.zentra.controller;

import com.priyakdey.zentra.exception.BadRequestException;
import com.priyakdey.zentra.model.request.NewAccountRequest;
import com.priyakdey.zentra.service.SignupService;
import com.priyakdey.zentra.validator.NewAccountRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;

import static com.priyakdey.zentra.validator.NewAccountRequestValidator.*;
import static com.priyakdey.zentra.validator.NewAccountRequestValidator.ValidationResult.SUCCESS;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;


/**
 * @author Priyak Dey
 */
@RestController
@RequestMapping(path = "/api/v1/signup", produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    private final SignupService signupService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SignupController(SignupService signupService, BCryptPasswordEncoder passwordEncoder) {
        this.signupService = signupService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<String> signup(@RequestBody NewAccountRequest newUser) {
        ValidationResult result = NewAccountRequestValidator.isValidUsername()
                .and(isValidEmail())
                .and(isValidPassword())
                .and(isValidName())
                .apply(newUser);

        if (result != SUCCESS) {
            throw new BadRequestException(result.getMessage());
        }

        char[] _password = newUser.getPassword();
        String password = passwordEncoder.encode(new String(_password));
        Arrays.fill(_password, '\0');

        int id = signupService.signup(newUser.getName(), newUser.getUsername(),
                password, newUser.getEmail());

        logger.info("New account created");
        return ResponseEntity.created(URI.create("/api/v1/users/" + id))
                .build();
    }

}
