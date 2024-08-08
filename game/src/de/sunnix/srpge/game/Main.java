package de.sunnix.srpge.game;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.InputManager;
import de.sunnix.srpge.engine.debug.profiler.Profiler;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.graphics.gui.text.Font;
import de.sunnix.srpge.engine.graphics.gui.text.Text;
import de.sunnix.srpge.engine.registry.Registry;
import de.sunnix.srpge.engine.stage.GameplayState;

import java.util.Arrays;

import static de.sunnix.srpge.engine.ecs.Direction.*;

public class Main {

    public static void main(String[] args) {

        Registry.addRegistry(new test.Registry());

        Core.setPixel_scale(3f);

        Core.init();
        Core.setPower_safe_mode(Arrays.stream(args).anyMatch("psm"::equalsIgnoreCase));
        Core.setVsync(Arrays.stream(args).anyMatch("vsync"::equalsIgnoreCase));
        Core.setDebug(Arrays.stream(args).anyMatch("debug"::equalsIgnoreCase));

        Core.createWindow("game", 1280, 720, null);

        Text.setDefaultFont(Font.ALUNDRA_FONT);

        var fpsText = new Text(" ");

        var playerCorrds = new Text(" ").setPos(0, fpsText.getPos().y + fpsText.getHeight());

        if(Arrays.stream(args).anyMatch("profiling"::equalsIgnoreCase)) {
            FlatDarkLaf.setup();
            Core.setUseProfiler(true);
            Profiler.createWindow();
        }

        Core.subscribeLoop("test", 4, ticks -> {
            if(ticks > 3){
                float h = 0;
                float v = 0;
                var jump = false;
                if(Core.hasFocus()) {
                    jump = InputManager.PAD_A.startPressed();

                    if(InputManager.PAD_RIGHT.isPressed())
                        h = 1;
                    if(InputManager.PAD_LEFT.isPressed())
                        h = -1;
                    if(h == 0)
                        h = InputManager.PAD_JS_L_H.getRight() - InputManager.PAD_JS_L_H.getLeft();
                    if(InputManager.PAD_DOWN.isPressed())
                        v = 1;
                    if(InputManager.PAD_UP.isPressed())
                        v = -1;
                    if(v == 0)
                        v = InputManager.PAD_JS_L_V.getRight() - InputManager.PAD_JS_L_V.getLeft();
                }
                if((h == 1 || h == -1) && (v == 1 || v == -1)){
                    h *= .707f;
                    v *= .707f;
                }
                var world = ((GameplayState)Core.GameState.GAMEPLAY.state).getWorld();
                var player = world.getPlayer();

                if(jump)
                    player.getComponent(PhysicComponent.class).jump();

                var pVel = player.getVelocity();
                var moveSpeed = .1f;
                pVel.set(h * moveSpeed, 0, v * moveSpeed);
                var pPos = player.getPosition();

                playerCorrds.change(tc -> tc.setText(String.format("Position: (%.2f, %.2f, %.2f) Z: %.5f", pPos.x, pPos.y, pPos.z, player.getZ_pos())));
                if(h != 0 || v != 0) {
                    player.addState(States.MOVING.id());
                    var comp = player.getComponent(RenderComponent.class);
                    if(Math.abs(h) > Math.abs(v))
                        if(h > 0)
                            comp.setDirection(EAST);
                        else
                            comp.setDirection(WEST);
                    else
                    if(v > 0)
                        comp.setDirection(SOUTH);
                    else
                        comp.setDirection(NORTH);
                } else
                    player.removeState(States.MOVING.id());

                fpsText.change(tc -> tc.setText(String.format("FPS: %.1f", Core.getFps())));

            }


        });

        Core.start();

    }
}
