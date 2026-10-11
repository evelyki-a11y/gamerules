package com.example.gamerules.game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GuardrailExceptionHandler {

    @ExceptionHandler(GuardrailViolationException.class)
    public ProblemDetail handle(GuardrailViolationException e) {
        // Sisendi blokeerimine on kliendi viga (400), väljundi blokeerimine meie poolne (502).
        // NB! Ära tagasta kliendile täpset põhjust - see aitab ründajal filtrit mööda hiilida.
        HttpStatus status = e.getStage() == GuardrailViolationException.Stage.INPUT
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.BAD_GATEWAY;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Question was rejected by a safety check.");
        problem.setProperty("guardrail", e.getGuardrail());
        problem.setProperty("stage", e.getStage());
        return problem;
    }
}
