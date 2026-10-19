# JavaFX con il pattern MVC

> Questo documento spiega come organizzare e strutturare un'applicazione JavaFX usando il pattern Model-View-Controller (MVC), concentrandosi sulla separazione della logica dai componenti della GUI.

## Layer Controller

Il layer controller contiene tutta la logica dell'applicazione. La struttura della directory è:

- `src/main/java/it/cookie/progetti/controllers`

## Layer View

Il layer view contiene i componenti dell'interfaccia grafica. La struttura della directory è:

- `src/main/resources/FXML`

Per trovare i file correlati, usa lo stesso percorso sia per la logica sia per la GUI. Per esempio:

- `src/main/java/it/cookie/progetti/controllers/MainMenu/MainMenuController.java`
- `src/main/resources/FXML/MainMenu/MainMenuView.fxml`

## Layer Utility

Alcuni file di utilità si trovano nella directory `utils`, ad esempio:

- `src/main/java/it/cookie/utils/network/user/UserNetController.java`

Questi file sono progettati per essere riutilizzati su piattaforme diverse, incluse applicazioni desktop e mobile. Per esempio, se un'app desktop e una per smartphone necessitano di una funzione di accesso, la logica dovrebbe restare coerente. Il `LoginController` dell'app desktop potrebbe essere:

```java
@FXML
@SuppressWarnings("unused")
private void handleLoginButtonAction() throws IOException {
    if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
        System.out.println("Username o password vuoti!");
        SceneManager.getInstance().showAlert(bundle.getString("login.Warning"), bundle.getString("login.MISSING_FIELDS"), AlertType.WARNING);
        return;
    }
    // Disabilita il pulsante per evitare richieste ripetute
    loginButton.setDisable(true);

    String username = usernameField.getText();
    String password = passwordField.getText();
    System.out.println("Tentativo di login con username: " + username);

    // Quando SessionManager è pronto, chiama questo update()
    SessionManager.getInstance().Attach(this);

    // Passa la richiesta a UserNetController ed esegue il login
    userNetController.UserLogin(username, password);
}
```

Questo metodo esegue i controlli e delega quindi la logica effettiva di accesso a `UserNetController`.

Il `LoginController` di un'app per smartphone implementerebbe controlli analoghi e delegherebbe la richiesta di accesso a `UserNetController`. Lo stesso flusso logico vale per i file nella directory `utils`.
