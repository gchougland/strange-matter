package com.hexvane.strangematter.platform;

/**
 * Holds client-only service implementations behind common interfaces.
 * Dedicated servers will never set these.
 */
public final class ClientServices {
    private ClientServices() {}

    private static AnomalySoundClient anomalySoundClient;

    public static void setAnomalySoundClient(AnomalySoundClient client) {
        anomalySoundClient = client;
    }

    public static AnomalySoundClient anomalySound() {
        return anomalySoundClient;
    }
}

