package com.google.firebase.messaging;

public class Notification {
    private String title;
    private String body;

    private Notification() {}

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Notification notification = new Notification();

        public Builder setTitle(String title) {
            notification.title = title;
            return this;
        }

        public Builder setBody(String body) {
            notification.body = body;
            return this;
        }

        public Notification build() {
            return notification;
        }
    }
}
