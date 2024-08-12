package com.woohaengshi.backend.controller;

import com.woohaengshi.backend.dto.response.timer.ShowTimerResponse;
import com.woohaengshi.backend.service.timer.TimerService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timer")
public class TimerController {

    private final TimerService timerService;

    @GetMapping
    public ShowTimerResponse showTimer() {
        return timerService.getTimer(1L);
    }
}
