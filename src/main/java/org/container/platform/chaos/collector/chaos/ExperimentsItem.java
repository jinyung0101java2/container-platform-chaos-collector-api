package org.container.platform.chaos.collector.chaos;

import lombok.Data;

import java.util.List;

@Data
public class ExperimentsItem {
    private long experimentTime;

    private Metadata metadata;
    private String kind;
    private Spec spec;

    @Data
    public class Metadata {
        private String creationTimestamp;
        private String name;
        private String namespace;
        private String uid;
    }

    @Data
    public class Spec {
        private String duration;
        private String action;
        private String gracePeriod;
        private String mode;
        private Selector selector;
        private Object stressors;
        private Delay delay;
    }

    @Data
    public class Delay {
        private String latency;
    }

    @Data
    public class Stressors {
        private ChaosCPU cpu;
        private ChaosMemory memory;
    }

    @Data
    public class ChaosCPU {
        private String load;
        private String workers;
    }

    @Data
    public class ChaosMemory {
        private String size;
        private String workers;
    }

    @Data
    public class Selector {
        private Object labelSelectors;
        private List namespaces;
        private Object pods;
    }
}
