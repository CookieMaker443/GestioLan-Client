package it.cookie.utils.network.managers;

import it.cookie.utils.interfaces.observer.Observer;
import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.user.user;

public class SessionManager extends Subject implements Observer{

    user current_user;
    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getUsername() {
        return current_user.getUsername();
    }

    private void setUsername(String username) {
        current_user.setUsername(username);
    }

    public String getEmail() {
        return current_user.getEmail();
    }

    private void setEmail(String email) {
        current_user.setEmail(email);
    }

    public String getJWT() {
        return current_user.getJWT();
    }

    private void setJWT(String JWT) {
        current_user.setJWT(JWT);
    }

    @Override
    public void Update(Subject subject, Object state) {
        // Aggiorna tutti i valori dopo aver aggiornato il database
        if(state instanceof user u) {
            this.current_user = u;
            Notify(u); // Successo
        } else {
            this.current_user = null;
            // passa la stringa di errore
            Notify(state); // Fallimento: sveglia il LoginController per riabilitare il tasto!
        }
    }

    public void ClearSession() {
        current_user = null;
    }

    public boolean IsUserLogged() {
        return current_user != null;
    }
}
