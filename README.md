# Sunnix's RPG Engine

This game engine was developed to create and play role-playing games with jump-n-run functions similar to Alundra.
The engine is currently still at an early stage, which means that essential functions may still be missing.
A rough overview of the available features can be found in the table below.

To download and try out the editor/engine, you can download it from this [Releases](https://github.com/Sunnnix/Sunnixs_RPG_Engine/releases/tag/V0.7) or from this website: [Download](https://sunnix.de/downloads).
If you don't know how to use the Editor you can check out the [Wiki](https://sunnix.de/wiki) and take a look the the getting started section.

> [!NOTE]
> The engine does not contain any graphics, music, or sounds; you have to add them yourself!

Version 0.7 is out now and I've created a video preview of the gameplay!

[![Version 0.7 Preview](https://img.youtube.com/vi/o6DPMTM28o4/0.jpg)](https://www.youtube.com/watch?v=o6DPMTM28o4)

## Patch Notes

<details>
    <summary>V0.7</summary>

### New Features
- **Global Variables** for conditional control of events.
- **Objects with Events and Components**:
    - New object shadow rendering.
    - Local Object Variables.
    - Objects can be enabled/disabled:
        - Disabled objects are not rendered, have no physics, and no events are executed (except for init run types).
    - **Events**:
        - Change Object Variable.
        - Change Global Variable.
        - Change Object State.
        - Change Object Properties (only enable/disable).
        - Global Tint.
        - Teleport.
        - Look At.
        - Lua Script Execution (Experimental).
        - Camera Control.
        - Change Tile (graphical changes only).
    - **Components**:
        - Physics Component for handling object collisions and interactions.
    - **Event List**:
        - Support for multiple event lists per object.
        - **Conditions**: Event lists only run if all conditions are met.
            - Number Condition (for global and local number variables).
        - **Run Types**:
            - Auto (Runs automatically).
            - Init (Runs once during world initialization).
            - Player Consult (Runs when player presses action in front of the object).
            - Touch (Runs when the player touches this object).
            - Step On (Runs only when the player moves downwards).
            - Touch Bottom (Runs when the player moves upwards).
            - Touch South (Runs when the player moves north).
            - Touch East (Runs when the player moves west).
            - Touch West (Runs when the player moves east).
            - Touch North (Runs when the player moves south).
        - Events can now run in parallel.

### User Interface Improvements
- **Textbox**: Added a Yes/No option for dialogues.
- **Tile Animation**: Animated tiles for dynamic environments.
- **Undo/Redo System**:
    - Support for undo/redo when drawing or copying tiles.
- **Copy/Paste System**:
    - Selected tiles in mode `F1` can now be copied and pasted.

### Map Editor Enhancements
- **Project Properties**:
    - Project name can be modified.
    - Option to display PlayStation or Xbox buttons in the text box.
- **New Option** to move wall textures up when changing wall size.
- **New Popup Menu**: Allows setting the player's starting position when editing objects.

### Performance and Stability
- **Render Ordering**: Fixed issues with render ordering.
- **Fill Option**: No longer works recursively, reducing memory usage and preventing stack overflow errors.
- **Engine Memory Usage**: Reduced by 65%.
- **Rendering Performance**: Massively improved.

### Miscellaneous
- Added a tile ladder option for better object traversal.

</details>

<details>
    <summary>V0.6</summary>

- Objects with events and components
    - Events
        - Move
        - Wait
        - Message
        - Play Sound
    - Components
        - Render
- Event controlled textbox
- Object animation V1
- Audio System for playing Sounds
- Object states

</details>

<details>
  <summary>V0.5</summary>
  
  - New loading dialog
  - Audio system
    - Audio files can now be loaded into the game file.
    - Maps can now have audio files assigned as background music.
  - Language packs (texts are now loaded from language packs, allowing the editor to support multiple languages)

</details>

<details>
  <summary>V0.4</summary>
  
  - With the CTRL key you can:
    - Scroll with the mouse wheel (initially only the map and not the tileset)
    - Drag with the left (primary) mouse button to move your view.
    - With the Shift key, you now have an additional layer per tile. This layer is drawn above the previous layer.
  - Additionally, you can now choose between SingleDraw (draw a single tile), DragFillDraw-Rect (drag from a start point to an endpoint and fill all tiles in between in a rectangle), and the normal Fill (like in Paint).
  - You can also toggle the grid on and off.
  - Finally, I have added options under the "Game" menu when opening the game.

</details>

<details>
  <summary>V0.3</summary>

  - Added start map
    - To run the game, you now have to select a start map.
    - The selected map is displayed in green.
    - This allows for testing each map individually.
  - Added tilesets
    - These are available under the Resource Manager.
  - The selected graphic of the tileset of maps now runs over the tilesets.

</details>

<details>
  <summary>V0.2</summary>

  - Added modules and modes
  - 3 Modes for:
    - (F1) Selecting tiles to manipulate them in height and, in the future, setting tile properties.
    - (F2) Drawing the top/ground of a tile.
    - (F3) Drawing the walls of tiles.
  - Added wall handling

</details>

## Supported Features

| Description                                           | Since |
|-------------------------------------------------------|-------|
| Tile animation                                        | 0.7   |
| Physics System                                        | 0.7   |
| Map Transition System / Teleport event                | 0.7   |
| Lua implementation for Events via JLua (Experimental) | 0.7   |
| Interacting System                                    | 0.7   |
| Audio System V2 (playable Sounds)                     | 0.6   |
| Sprite animation                                      | 0.6   |
| Dynamic Object properties via Components              | 0.6   |
| Object control via Events                             | 0.6   |
| Objects                                               | 0.6   |
| Audio System V1 (only BGM)                            | 0.4   |
| Editor multi-language support                         | 0.4   |
| Load custom tilesets                                  | 0.3   |
| Create 3D maps with walls                             | 0.2   |
| Start the game from the editor                        | 0.1   |

## Known Bugs
<details>
    <summary>Physic System</summary>

    A moving object can get stuck on a stair when moving against another object on that stair.
    Additionally, if an object runs up the stairs and collides with an object that is jumping, 
    the other objects get teleported above the jumping object.
    
</details>
<details>
    <summary>Editor</summary>

    Sometimes, parallel threads do not terminate correctly, causing the Java program to continue running. 
    In such cases, the program must be closed using the task manager.
</details>

## Upcoming Features

| Description                                                  | Planned | Progress | Priority |
|--------------------------------------------------------------|---------|----------|----------|
| Battle System                                                | 0.8     | 5%       | High     |
| Lua implementation for KI                                    | 0.8     | 0%       | Medium   |
| Object templates                                             | 0.8     | 0%       | Medium   |
| Copy Objects                                                 | 0.8     | 0%       | Medium   |
| Graphic Effects via Shader                                   | n/a     | 10%      | Medium   |
| Plugin system for editor and engine customization            | n/a     | 10%      | Low      |
| Inventory                                                    | n/a     | 0%       | High     |
| GUI                                                          | n/a     | 0%       | High     |
| Audio System V3 (more 3D options)                            | n/a     | 0%       | Medium   |
| Lua implementation for Events via JLua (Full implementation) | n/a     | 50%      | Low      |
| Python implementation for Events via Jython                  | n/a     | 0%       | Low      |
| Ruby implementation for Events via JRuby                     | n/a     | 0%       | Low      |
