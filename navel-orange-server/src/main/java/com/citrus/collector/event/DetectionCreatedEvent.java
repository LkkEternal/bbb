package com.citrus.collector.event;

import java.util.List;

public record DetectionCreatedEvent(List<Long> detectionIds) {
}
