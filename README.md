# Sunnix's RPG Engine

This game engine was developed to create and play role-playing games with jump-n-run functions similar to Alundra.
The engine is currently still at an early stage, which means that essential functions may still be missing.
A rough overview of the available features can be found in the table below.

To download and try out the editor/engine, you can download it from this [repo](https://github.com/Sunnnix/Sunnixs_RPG_Engine/releases/tag/V0.6) or from this website: [Download](https://sunnix.de/downloads).

> [!NOTE]
> The engine does not contain any graphics, music, or sounds; you have to add them yourself!

## Patch Notes

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

| Description                              | Since |
|------------------------------------------|-------|
| Audio System V2 (playable Sounds)        | 0.6   |
| Sprite animation                         | 0.6   |
| Dynamic Object properties via Components | 0.6   |
| Object control via Events                | 0.6   |
| Objects                                  | 0.6   |
| Audio System V1 (only BGM)               | 0.4   |
| Editor multi-language support            | 0.4   |
| Load custom tilesets                     | 0.3   |
| Create 3D maps with walls                | 0.2   |
| Start the game from the editor           | 0.1   |

## Upcoming Features

| Description                                                  | Planned | Progress | Priority |
|--------------------------------------------------------------|---------|----------|----------|
| Tile animation                                               | 0.7     | 100%     | Medium   |
| Physics System                                               | 0.7     | 100%     | High     |
| Map Transition System / Teleporter objects and events        | 0.7     | 100%     | High     |
| Lua implementation for Events via JLua (Experimental)        | 0.7     | 100%     | Medium   |
| Interacting System                                           | 0.7     | 93%      | High     |
| Lua implementation for KI                                    | n/a     | 0%       | Medium   |
| Graphic Effects via Shader                                   | n/a     | 10%      | Medium   |
| Plugin system for editor and engine customization            | n/a     | 10%      | Low      |
| Battle System                                                | n/a     | 0%       | High     |
| Audio System V3 (more 3D options)                            | n/a     | 0%       | Medium   |
| Lua implementation for Events via JLua (Full implementation) | n/a     | 50%      | Low      |
| Python implementation for Events via Jython                  | n/a     | 0%       | Low      |
| Ruby implementation for Events via JRuby                     | n/a     | 0%       | Low      |