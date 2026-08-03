package com.google.firebase.messaging;

public class Aps {
    private String sound;
    private String threadId;

    private Aps() {}

    public String getThreadId() {
        return threadId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Aps aps = new Aps();

        public Builder setSound(String sound) {
            aps.sound = sound;
            return this;
        }

        public Builder setThreadId(String threadId) {
            aps.threadId = threadId;
            return this;
        }

        public Aps build() {
            return aps;
        }
    }
}
