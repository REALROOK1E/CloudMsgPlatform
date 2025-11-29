package com.lzk.cloudmsg.enhance;

import com.lzk.cloudmsg.domain.MessageRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChainManager {

    private final List<EnhanceHandler> handlers;

    public Mono<Void> execute(MessageRecord messageRecord, Mono<Void> finalAction) {
        return new DefaultChain(handlers, 0, finalAction).next(messageRecord);
    }

    @RequiredArgsConstructor
    private static class DefaultChain implements EnhanceChain {
        private final List<EnhanceHandler> handlers;
        private final int index;
        private final Mono<Void> finalAction;

        @Override
        public Mono<Void> next(MessageRecord messageRecord) {
            if (index < handlers.size()) {
                return handlers.get(index).handle(messageRecord, new DefaultChain(handlers, index + 1, finalAction));
            } else {
                return finalAction;
            }
        }
    }
}
