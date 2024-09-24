package org.container.platform.chaos.collector.scheduler;

import lombok.Data;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;

import java.util.Map;

/**
 * ContainerMetrics 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-24
 */
@Data
public class ContainerMetrics {
    private String name;
    private Map<String, Quantity> usage;

    public String getName() {
        return name;
    }

    public Map<String, Quantity> getUsage() {
        return usage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsage(Map<String, Quantity> usage) {
        this.usage = usage;
    }
}

