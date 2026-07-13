package com.google.firebase.messaging;

public class AndroidConfig {
    private AndroidNotification notification;

    private AndroidConfig() {}

    public AndroidNotification getNotification() {
        return notification;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndroidConfig config = new AndroidConfig();

        public Builder setNotification(AndroidNotification notification) {
            config.notification = notification;
            return this;
        }

        public AndroidConfig build() {
            return config;
        }
    }
}
