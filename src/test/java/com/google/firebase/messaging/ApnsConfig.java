package com.google.firebase.messaging;

public class ApnsConfig {
    private Aps aps;

    private ApnsConfig() {}

    public Aps getAps() {
        return aps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ApnsConfig config = new ApnsConfig();

        public Builder setAps(Aps aps) {
            config.aps = aps;
            return this;
        }

        public ApnsConfig build() {
            return config;
        }
    }
}
