# JavaFX with the MVC Pattern

> This document explains how to organize and structure a JavaFX application using the Model-View-Controller (MVC) pattern, with a focus on separating logic from the GUI.

## Controller Layer

The controller layer contains all application logic. Its directory is:

- `src/main/java/it/cookie/progetti/controllers`

## View Layer

The view layer contains all graphical user-interface components. Its directory is:

- `src/main/resources/FXML`

To find related files, use the same path for both logic and GUI. For example:

- `src/main/java/it/cookie/progetti/controllers/MainMenu/MainMenuController.java`
- `src/main/resources/FXML/MainMenu/MainMenuView.fxml`

## Utility Layer

Some utility files are in the `utils` directory, for example:

- `src/main/java/it/cookie/utils/network/user/UserNetController.java`

These files are intended to be reusable across different platforms, including desktop and mobile applications. For example, if both a desktop and smartphone app need login functionality, their logic should remain consistent. The desktop application's `LoginController` could be:

```java
@FXML
@SuppressWarnings("unused")
private void handleLoginButtonAction() throws IOException {
    if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
        System.out.println("Username or password is empty!");
        SceneManager.getInstance().showAlert(bundle.getString("login.Warning"), bundle.getString("login.MISSING_FIELDS"), AlertType.WARNING);
        return;
    }
    // Disable the button to prevent spam
    loginButton.setDisable(true);

    String username = usernameField.getText();
    String password = passwordField.getText();
    System.out.println("Attempting login with username: " + username);

    // Once SessionManager is ready, call this update()
    SessionManager.getInstance().Attach(this);

    // Pass the login request to UserNetController and execute it
    userNetController.UserLogin(username, password);
}
```

This method performs validation, then delegates the actual login logic to `UserNetController`.

The `LoginController` of a smartphone application would implement similar checks and delegate the login request to `UserNetController`. The same logic applies to files in the `utils` directory.
