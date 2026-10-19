## Notes obtained with LLM help for documentation and/or learning

If the C# API uses an uppercase name (`Username`) while yours is lowercase, use this:

```java
@com.fasterxml.jackson.annotation.JsonProperty("Username")
public void SetUsername(String username) { this.username = username; }
```

## To transfer objects, you need to:

Create a DTO (an object representing what the API expects).

1. Create a key-value map of the objects to send.
2. Prepare the URL to which the request is sent.
3. Create a POST request.
4. Send the request (preferably asynchronously).
5. Read the response and turn it into an object (such as `User`).
6. Optionally call `notify`.

Example with `User.java`:

`import com.fasterxml.jackson.annotation.JsonIgnoreProperties;`

### It tells Jackson to ignore fields that are not present in this class

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class User { /* ... */ }
```

### Use `@JsonProperty` to map the exact JSON field name

```java
@JsonProperty("username")
private String username;
```

`@JsonIgnoreProperties(ignoreUnknown = true)` is essential. When the API sends `"password": null`, Jackson will say: “I do not know where to put it, so I will ignore it rather than fail.”

`@JsonProperty("username")` forces Jackson to bind the JSON key directly to the field, regardless of whether the method is named `GetUsername` or `getUsername`.

`LocalDateTime`: the API sends an ISO value such as `2025-12-27T14:28:59`. `java.sql.Date` handles only the date (year-month-day) and often fails with complete timestamps. Using `LocalDateTime` and the `JavaTimeModule` configured earlier on the `ObjectMapper` handles it correctly.

## Client side

This approach also makes sense on the client side.

#### Querying multiple categories

- In the left tab, select category icons.
- When X categories are selected and the query is sent, the client sends an integer array representing the chosen categories.
- The API retrieves the matching items and returns them.
- The client must then use the result to display the correct icons.

#### Displaying the correct icons in a panel

- `ItemContainerController` must take the integer of its item, perform a bitwise check against the category map (already loaded from the database when the software starts), and add the matching `Label` for every category that evaluates to `true`.
  - If the id is `0`, the item has no category and receives a default label.
- Keep the displayed `Label` objects in memory.
- When an item changes, update its labels using the method described above.
