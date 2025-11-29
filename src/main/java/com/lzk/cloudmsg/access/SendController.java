package com.lzk.cloudmsg.access;

import com.lzk.cloudmsg.access.dto.MessageRequest;
import com.lzk.cloudmsg.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/send")
@RequiredArgsConstructor
public class SendController {

    private final SendService sendService;

    @PostMapping("/batch")
    public Mono<Response<String>> sendBatch(@RequestBody MessageRequest request) {
        return sendService.send(request)
                .map(Response::success)
                .onErrorResume(e -> Mono.just(Response.fail(e.getMessage())));
    }
}
