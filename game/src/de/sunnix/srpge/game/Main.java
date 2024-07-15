package de.sunnix.srpge.game;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.InputManager;
import de.sunnix.srpge.engine.debug.profiler.Profiler;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.graphics.gui.text.Font;
import de.sunnix.srpge.engine.graphics.gui.text.Text;
import de.sunnix.srpge.engine.registry.Registry;
import de.sunnix.srpge.engine.stage.GameplayState;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        Registry.addRegistry(new test.Registry());

        Core.setPixel_scale(3f);

        Core.init();
        Core.createWindow(1280, 720);

        Core.setPower_safe_mode(Arrays.stream(args).anyMatch("psm"::equalsIgnoreCase));
        Core.setVsync(Arrays.stream(args).anyMatch("vsync"::equalsIgnoreCase));

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
                var y = 0;
                if(Core.hasFocus()) {
                    y += InputManager.PAD_X.isPressed() ? -1 : 0;
                    y += InputManager.PAD_B.isPressed() ? 1 : 0;

                    h = InputManager.PAD_JS_L_H.getRight() - InputManager.PAD_JS_L_H.getLeft();
                    v = InputManager.PAD_JS_L_V.getRight() - InputManager.PAD_JS_L_V.getLeft();
                }
                var world = ((GameplayState)Core.GameState.GAMEPLAY.state).getWorld();
                var player = world.getPlayer();
                var pVel = player.getVelocity();
                pVel.set(h * .075f, y * .01f, v * .075f);
                var pPos = player.getPosition();

                playerCorrds.change(tc -> tc.setText(String.format("Position: (%.2f, %.2f, %.2f) Z: %.5f", pPos.x, pPos.y, pPos.z, player.getZ_pos())));
                if(h != 0 || v != 0) {
                    player.addState(States.MOVING.id());
                    var comp = player.getComponent(RenderComponent.class);
                    if(Math.abs(h) > Math.abs(v))
                        if(h > 0)
                            comp.setDirection(2);
                        else
                            comp.setDirection(1);
                    else
                    if(v > 0)
                        comp.setDirection(0);
                    else
                        comp.setDirection(3);
                } else
                    player.removeState(States.MOVING.id());

                fpsText.change(tc -> tc.setText(String.format("FPS: %.1f", Core.getFps())));

            }


        });

        Core.start();

    }
}
