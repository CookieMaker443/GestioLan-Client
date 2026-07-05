package it.cookie.utils.network.user;

/**
 * Rappresenta un errore di login.
 * - "known": errore previsto, identificato da una chiave del ResourceBundle (es. "login.SERVER_TIMEOUT")
 * - "unknown": errore non previsto, contiene il messaggio grezzo della JVM/eccezione
 *
 * Distinguere i due casi con un tipo dedicato (invece che con trucchi sul contenuto
 * di una String, tipo base64) permette di usare instanceof in modo affidabile:
 * LoginError è sempre e solo LoginError, indipendentemente dal contenuto.
 */
public record MessageText(String bundleKey, String rawMessage) {

    public static MessageText known(String bundleKey) {
        return new MessageText(bundleKey, null);
    }

    public static MessageText unknown(String rawMessage) {
        return new MessageText(null, rawMessage);
    }

    public boolean isKnown() {
        return bundleKey != null;
    }
}