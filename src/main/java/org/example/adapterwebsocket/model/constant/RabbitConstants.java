package org.example.adapterwebsocket.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RabbitConstants {

    // --- QUEUE ---
    public static final String Q_CRYPTO_RATE_LIVE = "dbs_crypto_rate_live_q";

    // --- EXCHANGE ---
    public static final String X_DBS_WEBSOCKET = "dbs.websocket.dx";

    // --- ROUTE ---
    public static final String K_CRYPTO_RATE_LIVE = "crypto.rate.live";

}
