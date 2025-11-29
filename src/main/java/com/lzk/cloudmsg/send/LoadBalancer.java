package com.lzk.cloudmsg.send;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class LoadBalancer {

    private final Map<Integer, List<WeightedChannel>> channelMap = new ConcurrentHashMap<>();

    public void init(List<Channel> channels) {
        Map<Integer, List<Channel>> grouped = channels.stream()
                .collect(Collectors.groupingBy(Channel::getChannelType));
        
        grouped.forEach((type, list) -> {
            List<WeightedChannel> weightedList = list.stream()
                    .map(c -> new WeightedChannel(c, 0, c.getWeight()))
                    .collect(Collectors.toList());
            channelMap.put(type, weightedList);
        });
    }

    public Channel selectChannel(Integer channelType) {
        List<WeightedChannel> channels = channelMap.get(channelType);
        if (channels == null || channels.isEmpty()) {
            return null;
        }

        WeightedChannel selected = null;
        double totalEffectiveWeight = 0;

        synchronized (channels) {
            for (WeightedChannel wc : channels) {
                totalEffectiveWeight += wc.getEffectiveWeight();
                wc.setCurrentWeight(wc.getCurrentWeight() + wc.getEffectiveWeight());

                if (selected == null || wc.getCurrentWeight() > selected.getCurrentWeight()) {
                    selected = wc;
                }
            }

            if (selected != null) {
                selected.setCurrentWeight(selected.getCurrentWeight() - totalEffectiveWeight);
                return selected.getChannel();
            }
        }
        return channels.get(0).getChannel();
    }
    
    // Method to update effective weight based on performance (mock)
    public void updateWeight(String channelName, double newWeight) {
        // Iterate all lists to find channel
        channelMap.values().forEach(list -> {
            list.stream()
                .filter(wc -> wc.getChannel().getName().equals(channelName))
                .findFirst()
                .ifPresent(wc -> wc.setEffectiveWeight(newWeight));
        });
    }
}
