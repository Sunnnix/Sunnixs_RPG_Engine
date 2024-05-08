package de.sunnix.aje.game;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.aje.engine.Core;
import de.sunnix.aje.engine.InputManager;
import de.sunnix.aje.engine.debug.profiler.Profiler;
import de.sunnix.aje.engine.graphics.Camera;
import de.sunnix.aje.engine.graphics.Texture;
import de.sunnix.aje.engine.graphics.gui.GUIManager;
import de.sunnix.aje.engine.graphics.gui.text.Font;
import de.sunnix.aje.engine.graphics.gui.text.Text;
import de.sunnix.aje.engine.registry.IRegistry;
import de.sunnix.aje.engine.registry.Registry;
import de.sunnix.aje.engine.stage.GameplayState;

import java.util.Arrays;

public class Main implements IRegistry {

    // Tilesize = 24 * 16

    static boolean first = true;

    private static int t = 0;

    private static Texture ALUNDRA;

    public static void main(String[] args) {

        Registry.addRegistry(new test.Registry());

        Core.setPixel_scale(3f);

        Core.init();
        Core.createWindow(1280, 720);

        Core.setPower_safe_mode(Arrays.stream(args).anyMatch("psm"::equalsIgnoreCase));
        Core.setVsync(Arrays.stream(args).anyMatch("vsync"::equalsIgnoreCase));

//        text.setPos(0, 5);

//        new TextBox("Test text 123", 0, 0, 0, 0);

        ALUNDRA = new Texture("/assets/textures/entity/player/alundra_idle.png");

        Registry.addRegistry(new Main());

        Text.setDefaultFont(Font.ALUNDRA_FONT);

        var fpsText = new Text(" ");

        var playerCorrds = new Text(" ").setPos(0, fpsText.getPos().y + fpsText.getHeight());

        var exampleText = new Text("Example test, to see how this Font is working out!").setPos(0, playerCorrds.getPos().y + playerCorrds.getHeight());

        var text = new Text("" +
                Text.ARROW_RIGHT + Text.ARROW_LEFT + Text.ARROW_UP + Text.ARROW_DOWN +
                Text.XBOX_X + Text.XBOX_Y + Text.XBOX_B + Text.XBOX_A +
                Text.PS_RECT + Text.PS_TRI + Text.PS_CIR + Text.PS_X);
        text.setPos(0, exampleText.getPos().y + exampleText.getHeight());

        var arrow = new Text(String.valueOf(Text.CURSOR_0));
        arrow.setPos(text.getWidth() / 2 - arrow.getWidth() / 2, text.getPos().y + text.getHeight());

//        for (int i = 0; i < 1000; i++)
//            new Text("Test Lorem ipsum").setPos(200, 100);

        if(Arrays.stream(args).anyMatch("profiling"::equalsIgnoreCase)) {
            FlatDarkLaf.setup();
            Core.setUseProfiler(true);
            Profiler.createWindow();
        }

//        var scroll = new TextureRenderObject(SCROLL);
//        var scrollFactor = (float)SCROLL.getHeight() / SCROLL.getWidth();
//        scroll.getSize().set(Camera.getSize().x / Core.getPixel_scale(), Camera.getSize().x / Core.getPixel_scale() * scrollFactor);

//        var scrollText = new Text(
//                """
//                        Test text 123
//                        Next line test!
//                        Third line!
//                        """,
//                tc -> tc.setColor(.15f, .17f, .1f, 1f)
//                );

        Core.subscribeLoop("test", 4, ticks -> {
            if(InputManager.PAD_A.startPressed())
//                GUIManager.showSpeechBox("Sunnix", "Kleiner test!");//\nZweite Linie! (ZONK)\nDrücke " + PS_X +  STOP_TOKEN + " um fortzufahren!\nZum Abbrechen drücke " + PS_RECT + "!\nAnsonsten kannst du dieses Spiel jetzt beenden.");
//                GUIManager.showSpeechBox("Sunnix", String.format("""
//                        Lorem ipsum dolor sit amet, %s consetetur sadipscing elitr,
//                        sed diam nonumy eirmod tempor invidunt ut labore et %s
//                        dolore magna aliquyam erat, sed diam voluptua.
//                        At vero eos et accusam et justo duo dolores et ea rebum.
//                        Stet clita kasd gubergren, no sea takimata sanctus est
//                        Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet,
//                        consetetur sadipscing elitr, sed diam nonumy eirmod %s
//                        tempor invidunt ut labore et dolore magna aliquyam
//                        erat, %s%s
//                        sed diam voluptua. At vero eos et accusam et justo duo
//                        dolores et ea rebum. Stet clita kasd gubergren,
//                        no sea takimata sanctus est Lorem ipsum dolor sit
//                        amet. %s""", STOP_TOKEN, PS_X, PS_RECT, PS_TRI, PS_X, PS_CIR));


            GUIManager.showSpeechBox("Sunnix", """
                        Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
                        sed diam nonumy eirmod tempor invidunt ut labore et
                        dolore magna aliquyam erat, sed diam voluptua.
                        At vero eos et accusam et justo duo dolores et ea rebum.""");

//                if(!GUIManager.isTextFinished())
//                    GUIManager.scrollNext();
//                else if(GUIManager.isScrollVisible())
//                    GUIManager.scrollScroll();
//                else
//                    GUIManager.showScroll("Kleiner test!\nZweite Linie! (ZONK)\nDrücke " + PS_X + " um fortzufahren!");
            // Hier wird der Loop ausgeführt!
            fpsText.change(tc -> tc.setText(String.format("FPS: %s", Math.round(Core.getFps()))));
            if(ticks % 10 == 0) {
                t++;
                arrow.change(tc -> tc.setText(String.valueOf((char)(Text.CURSOR_0 + t % 4))));
            }

            if(ticks == 3){
//                var go = new GameObject();
//                go.addComponent(Component.RENDER);
//                RenderComponent.TEXTURE.set(go, "alundra");
//                go.init();
            }
            if(ticks > 1) {
//                scroll.render(new Vector3f(0, -Camera.getSize().x / Core.getPixel_scale() * scrollFactor , 0));
//                scrollText.setPos(35, 172);
            }

            if(ticks > 3){
                float h = 0;
                float v = 0;
                var y = 0;
//                h += InputManager.PAD_LEFT.isPressed() ? -1 : 0;
//                h += InputManager.PAD_RIGHT.isPressed() ? 1 : 0;
//                v += InputManager.PAD_UP.isPressed() ? -1 : 0;
//                v += InputManager.PAD_DOWN.isPressed() ? 1 : 0;
                if(Core.hasFocus()) {
                    y += InputManager.PAD_X.isPressed() ? -1 : 0;
                    y += InputManager.PAD_B.isPressed() ? 1 : 0;

                    h = InputManager.PAD_JS_L_H.getRight() - InputManager.PAD_JS_L_H.getLeft();
                    v = InputManager.PAD_JS_L_V.getRight() - InputManager.PAD_JS_L_V.getLeft();
                }
                var world = ((GameplayState)Core.GameState.GAMEPLAY.state).getWorld();
                var pPos = world.getPlayer().getPosition();
                pPos.add(h * .05f, y * .01f, v * .05f);
                pPos.set(pPos.x, Math.max(pPos.y, 0), pPos.z);
                Camera.getPos().set(pPos.x * 24, (-pPos.z + pPos.y) * 16);

                playerCorrds.change(tc -> tc.setText(String.format("(%.2f, %.2f, %.2f) Z: %.5f", pPos.x, pPos.y, pPos.z, world.getPlayer().getZ_pos())));
            }


        });

        Core.start();

    }

    @Override
    public void register() {
        Registry.TEXTURE.register("alundra", ALUNDRA);
    }
}
