package com.google.firebase.messaging;

public class AndroidNotification {
    private String sound;
    private String tag;

    private AndroidNotification() {}

    public String getTag() {
        return tag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndroidNotification notification = new AndroidNotification();

        public Builder setSound(String sound) {
            notification.sound = sound;
            return this;
        }

        public Builder setTag(String tag) {
            notification.tag = tag;
            return this;
        }

        public AndroidNotification build() {
            return notification;
        }
    }
}
