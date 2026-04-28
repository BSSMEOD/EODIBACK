package com.eod.eod.domain.discord.presentation;

import com.eod.eod.domain.discord.application.DiscordVerifyService;
import com.eod.eod.domain.discord.application.PickupDateService;
import com.eod.eod.domain.discord.presentation.dto.PickupDateRequest;
import com.eod.eod.domain.discord.presentation.dto.PickupDateResponse;
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
@RequestMapping("/discord")
@RequiredArgsConstructor
public class DiscordController {

    private final DiscordVerifyService discordVerifyService;
    private final PickupDateService pickupDateService;

    @PostMapping("/verify")
    public ResponseEntity<DiscordVerifyResponse> verify(@Valid @RequestBody DiscordVerifyRequest request) {
        DiscordVerifyResponse response = discordVerifyService.verify(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pickup-date")
    public ResponseEntity<PickupDateResponse> savePickupDate(@Valid @RequestBody PickupDateRequest request) {
        pickupDateService.savePickupDate(request.discordId(), request.pickupDate());
        return ResponseEntity.ok(new PickupDateResponse(true, "날짜가 등록되었습니다."));
    }
}
