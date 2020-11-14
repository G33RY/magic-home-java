# Magic Home for Java 11+
The functionality was ported from https://github.com/jangxx/node-magichome to Java 11+

## Features
Control MagicHue lights

**Controller:** Turn the lights on and off. Set Colors. Store a name to use with a controller      
**Discover:** Discover Magic Home lights on the network

## Usage
You can download the JAR library from the releases page or [here](https://github.com/G33RY/magic-home-java/releases/tag/1.0).
Then you can import it to your Java 11+ Project. 

##### Importing the library to Intellj IDEA
**File** --> **Project Structure** --> **Libraries** --> **Press the + button** --> **Select JAR File** --> **OK**


##### Basic example
```
Controller[] controllers = Discover.Scan(); // Scan for available controllers on the network

String ip = controllers[0].getIP(); // Get controller's IP
controllers[0].setColor(255, 0, 0); // Change the color of the controller to red
controllers[0].setWarmWhite(255); // Change controller to warm white (if available)
controllers[0].setColdWhite(255); // Change controller to cold white (if available)
controllers[0].setPower(false); // Turn off controller
```

## Methods

- #### Controller
    - ##### getIP()
        - Get the IP of the controller
        - **Params:** -
        - **Return:**  String
    - ##### getID()
        - Get the ID of the controller
        - **Params:** -
        - **Return:**  String
    - ##### getMODEL()
        - Get the Model of the controller
        - **Params:** -
        - **Return:** String
    - ##### getName()
        - Get the Name of the controller
        - **Params:** -
        - **Return:**  String
    - ##### setName(name)
        - Set the Name of the controller and store it
        - **Params:** name: *String*
        - **Return:**  -
    - ##### setPower(on)
        - Turn on and off the controller
        - **Params:** on: *Boolean*
        - **Return:**  *TRUE* successful, *FALSE* if failed
    - ##### setColor(red, green, blue)
        - Set the RGB color of the controller 
        - **Params:** red: *Integer(0-255)*, green: *Integer(0-255)*, blue: *Integer(0-255)*
        - **Return:**  *TRUE* successful, *FALSE* if failed
    - ##### setWarmWhite(ww)
        - Set the Warm White value of the controller  (if available)
        - **Params:** ww: *Integer(0-255)*
        - **Return:**  *TRUE* successful, *FALSE* if failed
    - ##### setColdWhite(cw)
        - Set the Cold White value of the controller (if available)
        - **Params:** cw: *Integer(0-255)*
        - **Return:**  *TRUE* successful, *FALSE* if failed
    - ##### queryState()
        - Query the properties of the controller
        - **Params:** -
        - **Return:** HashMap(key: *String*, value: *Object*)
            ```
            type: Integer
            on: Boolean
            mode: String (color / ia_pattern / pattern / custom / special)
            pattern: String (One of the hard-coded pattern's name)
            speed: Integer
            colors.red: Integer
            colors.green: Integer
            colors.blue: Integer
            warm_white: Integer
            cold_white: Integer
            ```
    - ##### validateColors()
        - Check if the controller color values matches the stored values
        - **Params:** -
        - **Return:**  *TRUE* everything matches, *FALSE* something did not match
- #### Discover
    - ##### Scan()
        - Search for available controllers
        - **Params:** -
        - **Return:**  Array of *Controller* class
