package com.lzk.cloudmsg.send;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeightedChannel {
    private Channel channel;
    private double currentWeight;
    private double effectiveWeight;
}
