package com.lzk.cloudmsg.access;

import com.alibaba.fastjson2.JSON;
import com.lzk.cloudmsg.access.dto.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendServiceImpl implements SendService {

    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "cloud-msg-access";

    @Override
    public Mono<String> send(MessageRequest request) {
        if (request.getCrowdKey() != null) {
            return handleCrowdRequest(request);
        } else {
            return handleDirectRequest(request);
        }
    }

    private Mono<String> handleDirectRequest(MessageRequest request) {
        // Generate a Business ID if not present
        if (request.getBusinessId() == null) {
            request.setBusinessId(UUID.randomUUID().toString());
        }
        
        // In a real scenario, we might split large receiver lists here too
        String json = JSON.toJSONString(request);
        return kafkaTemplate.send(TOPIC, request.getBusinessId(), json)
                .map(senderResult -> request.getBusinessId())
                .doOnError(e -> log.error("Failed to send to Kafka", e));
    }

    private Mono<String> handleCrowdRequest(MessageRequest request) {
        log.info("Processing crowd request for key: {}", request.getCrowdKey());
        
        // Simulate downloading bitmap file
        return Mono.fromCallable(() -> {
            // In reality: Download file from MinIO
            // Here: Create a dummy bitmap for demonstration
            org.roaringbitmap.RoaringBitmap bitmap = new org.roaringbitmap.RoaringBitmap();
            for (int i = 1; i <= 10005; i++) {
                bitmap.add(i);
            }
            return bitmap;
        }).flatMap(bitmap -> {
            // Iterate and batch
            org.roaringbitmap.IntIterator iterator = bitmap.getIntIterator();
            List<String> batch = new ArrayList<>();
            List<Mono<String>> sendMonos = new ArrayList<>();
            
            while (iterator.hasNext()) {
                batch.add(String.valueOf(iterator.next()));
                if (batch.size() >= 1000) {
                    sendMonos.add(sendBatch(request, new ArrayList<>(batch)));
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                sendMonos.add(sendBatch(request, new ArrayList<>(batch)));
            }
            
            return Flux.merge(sendMonos)
                    .then(Mono.just(request.getCrowdKey())); // Return crowdKey as task ID
        });
    }

    private Mono<String> sendBatch(MessageRequest originalRequest, List<String> receivers) {
        MessageRequest batchRequest = MessageRequest.builder()
                .templateId(originalRequest.getTemplateId())
                .businessId(UUID.randomUUID().toString())
                .variables(originalRequest.getVariables())
                .receivers(new java.util.HashSet<>(receivers))
                .crowdKey(originalRequest.getCrowdKey()) // Keep track
                .build();
        return handleDirectRequest(batchRequest);
    }
}
