package com.eod.eod.domain.discord.presentation;

import com.eod.eod.domain.discord.application.DiscordVerifyService;
import com.eod.eod.domain.discord.presentation.dto.request.DiscordVerifyRequest;
import com.eod.eod.domain.discord.presentation.dto.response.DiscordVerifyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discord")
public class DiscordController {

    private final DiscordVerifyService discordVerifyService;

    @PostMapping("/verify")
    public ResponseEntity<DiscordVerifyResponse> verify(@Valid @RequestBody DiscordVerifyRequest request) {
        DiscordVerifyResponse response = discordVerifyService.verify(request);
        return ResponseEntity.ok(response);
    }
}
