To implement a container-management system for items, follow this workflow:

1. Define the classes that manage containers and data.
2. Create controller and manager instances.
3. Implement methods for collecting data and updating containers.
4. Test the system for correctness and performance.

For the container-management class, implement these methods:

* `__construct()`: class constructor that initializes the container map.
* `addContentContainer()`: adds a new container to the container map.
* `removeContentContainer()`: removes a container from the map.
* `getContentContainer()`: returns the container for the supplied ID.
* `updateContentContainers()`: updates containers with the latest information.

For the data-management class, implement these methods:

* `__construct()`: class constructor that initializes the information buffer and container map.
* `addItem()`: adds a new item to the loaded-items list.
* `removeItem()`: removes an item from the loaded-items list.
* `getItems()`: returns the list of loaded items.
* `updateItems()`: updates items with the latest information.

For the controller-management class, implement these methods:

* `__construct()`: class constructor that initializes the container manager and user-interface objects.
* `handleRequest()`: handles user requests and uses the container manager to collect the required data and update the corresponding containers.

For the user-interface management class, implement these methods:

* `__construct()`: class constructor that initializes the container manager and user-interface objects.
* `showContentContainer()`: shows the container for the supplied ID.
* `updateContentContainers()`: updates containers with the latest information.

To implement reload logic, create a separate function that handles the reload action and uses the container manager to update the relevant containers.

In short, create the container- and data-management classes, implement their data-collection and container-update methods, and test the system for correctness and performance.
