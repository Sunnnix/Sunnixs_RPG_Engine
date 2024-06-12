package de.sunnix.srpge.engine.graphics.gui;

import de.sunnix.srpge.engine.GlobalConfig;
import de.sunnix.srpge.engine.InputManager;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.gui.text.Text;
import lombok.Getter;

import java.util.Arrays;

import static de.sunnix.srpge.engine.graphics.gui.text.Text.*;

public class SpeechBox {

    private static final int NULL = 0;
    private static final int FADE_IN = 1;
    private static final int FADE_OUT = 2;
    private static final int ANIMATE_TEXT = 3;
    private static final int ANIMATE_TEXT_SHIFT = 4;
    private static final float[] textColor = { .15f, .17f, .1f, 1f };
    @Getter
    private int id;
    private String name, text;
    private final TextBoxRenderObject scroll = new TextBoxRenderObject();
    private final NameBoxRenderObject nameBoxSmall = new NameBoxRenderObject(scroll, 0);
    private final NameBoxRenderObject nameBoxMedium = new NameBoxRenderObject(scroll, 1);
    private final NameBoxRenderObject nameBoxLarge = new NameBoxRenderObject(scroll, 2);
    private NameBoxRenderObject currentNameBox = nameBoxSmall;
    private final Text first_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text second_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text third_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text text_arrow = new Text(false, tc -> tc.setText(Character.toString(CURSOR_0)).setExceptionalColoringChars(CURSOR_0, CURSOR_3 + 1)).setSize(10);
    private final Text name_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);

    @Getter
    private boolean visible;
    @Getter
    private boolean finished = true;
    private int action;

    private final float maxYShift = scroll.getSize().y * 1.5f;
    private final float scrollSpeedY = maxYShift / 20;
    private final float scrollSpeedX = Camera.getSize().x / 20;
    private int timer;

    private int cursorPos;

    public SpeechBox() {
        first_line.setPos(26, 182 + scroll.getOffsetY());
        second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight());
        third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
        text_arrow.setPos(Camera.getSize().x - 30, third_line.getPos().y - 10 - scroll.getOffsetY());
    }

    public void render(){
        if(visible) {
            timer++;
            scroll.render();
            if(name != null){
                currentNameBox.render();
                name_line.setPos(currentNameBox.getPos().x + currentNameBox.getSize().x / 2 - name_line.getWidth() / 2 + currentNameBox.getOffsetX(), currentNameBox.getPos().y + 87);
            }
            if(name != null)
                name_line.render();
            if(action != FADE_IN)
                renderText();
        } else
            timer = 0;
        update();
    }

    public void showText(int id, String name, String text){
        if(this.visible)
            throw new IllegalStateException("Can't create new text when speech box is showing");
        this.finished = false;
        this.cursorPos = 0;
        this.visible = true;

        // convert buttons
        if(GlobalConfig.isPsMode()){
            text = text.replaceAll(String.valueOf(XBOX_X), String.valueOf(PS_RECT));
            text = text.replaceAll(String.valueOf(XBOX_Y), String.valueOf(PS_TRI));
            text = text.replaceAll(String.valueOf(XBOX_B), String.valueOf(PS_CIR));
            text = text.replaceAll(String.valueOf(XBOX_A), String.valueOf(PS_X));
        } else {
            text = text.replaceAll(String.valueOf(PS_RECT), String.valueOf(XBOX_X));
            text = text.replaceAll(String.valueOf(PS_TRI), String.valueOf(XBOX_Y));
            text = text.replaceAll(String.valueOf(PS_CIR), String.valueOf(XBOX_B));
            text = text.replaceAll(String.valueOf(PS_X), String.valueOf(XBOX_A));
        }

        this.id = id;
        this.name = name == null || name.isBlank() ? null : name;
        this.text = text;

        // set name box
        this.name_line.change(tc -> tc.setText(name));
        if(this.name_line.getWidth() > 120)
            this.currentNameBox = this.nameBoxLarge;
        else if(this.name_line.getWidth() > 68)
            this.currentNameBox = this.nameBoxMedium;
        else
            this.currentNameBox = this.nameBoxSmall;

        this.action = FADE_IN;
//
//        if(action != NULL)
//            return;
//        if (visible)
//            if (finished)
//                action = FADE_OUT;
//            else
//                action = ANIMATE_TEXT;
//        else {
//            action = FADE_IN;
//            if(GlobalConfig.isPsMode()){
//                text = text.replaceAll(String.valueOf(XBOX_X), String.valueOf(PS_RECT));
//                text = text.replaceAll(String.valueOf(XBOX_Y), String.valueOf(PS_TRI));
//                text = text.replaceAll(String.valueOf(XBOX_B), String.valueOf(PS_CIR));
//                text = text.replaceAll(String.valueOf(XBOX_A), String.valueOf(PS_X));
//            } else {
//                text = text.replaceAll(String.valueOf(PS_RECT), String.valueOf(XBOX_X));
//                text = text.replaceAll(String.valueOf(PS_TRI), String.valueOf(XBOX_Y));
//                text = text.replaceAll(String.valueOf(PS_CIR), String.valueOf(XBOX_B));
//                text = text.replaceAll(String.valueOf(PS_X), String.valueOf(XBOX_A));
//            }
//            this.text = text;
//            this.name = name;
//            this.name_line.change(tc -> tc.setText(name));
//            if(name_line.getWidth() > 120)
//                currentNameBox = nameBoxLarge;
//            else if(name_line.getWidth() > 68)
//                currentNameBox = nameBoxMedium;
//            else
//                currentNameBox = nameBoxSmall;
//            cursorPos = 0;
//            finished = false;
//            visible = true;
//        }
    }

    private void renderText(){
        if(!first_line.getText().isEmpty())
            first_line.render();
        if(!second_line.getText().isEmpty())
            second_line.render();
        if(!third_line.getText().isEmpty())
            third_line.render();
        if(action == NULL)
            text_arrow.render();
    }

    private void update(){
        switch (action){
            case FADE_IN -> updateFadeIn();
            case FADE_OUT -> updateFadeOut();
            case ANIMATE_TEXT, ANIMATE_TEXT_SHIFT -> updateText();
            case NULL -> {
                updateArrow();
                if(InputManager.PAD_A.startPressed())
                    if(finished)
                        action = FADE_OUT;
                    else
                        action = ANIMATE_TEXT;
            }
        }
    }

    private void updateFadeIn(){
        scroll.setOffsetY(scroll.getOffsetY() - scrollSpeedY);
        if(name != null)
            currentNameBox.setOffsetX(currentNameBox.getOffsetX() - scrollSpeedX);
        first_line.getPos().add(0, -scrollSpeedY);
        second_line.getPos().add(0, -scrollSpeedY);
        third_line.getPos().add(0, -scrollSpeedY);
        if(scroll.getOffsetY() <= 0)
            action = ANIMATE_TEXT;
    }

    private void updateFadeOut() {
        scroll.setOffsetY(scroll.getOffsetY() + scrollSpeedY);
        if(name != null)
            currentNameBox.setOffsetX(currentNameBox.getOffsetX() + scrollSpeedX);
        first_line.getPos().add(0, scrollSpeedY);
        second_line.getPos().add(0, scrollSpeedY);
        third_line.getPos().add(0, scrollSpeedY);
        if(scroll.getOffsetY() >= maxYShift) {
            action = NULL;
            first_line.change(tc -> tc.setText(""));
            second_line.change(tc -> tc.setText(""));
            third_line.change(tc -> tc.setText(""));
            visible = false;
        }
    }

    private int shiftAnimation = 0;

    private void updateText(){
        var ignoreCheck = false;
        if(action == ANIMATE_TEXT_SHIFT){
            var maxShiftAnim = 20;
            shiftAnimation += (InputManager.PAD_A.isPressed() ? 3 : 1);
            var animProgress = 1f / maxShiftAnim * shiftAnimation;
            if(shiftAnimation < maxShiftAnim) {
                first_line.change(tc -> tc.setColor(textColor[0], textColor[1], textColor[2], 1 - animProgress * 2));
                second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight() * (1 - animProgress));
                third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
                return;
            } else {
                action = ANIMATE_TEXT;
                first_line.change(tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setText(second_line.getText()));
                second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight());
                second_line.change(tc -> tc.setText(third_line.getText()));
                third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
                third_line.change(tc -> tc.setText(""));
            }
        }
        if(!ignoreCheck && (!InputManager.PAD_A.isPressed() && timer % 6 != 0))
            return;
        cursorPos++;
        if(cursorPos == text.length() + 1) {
            finished = true;
            action = NULL;
        } else {
            var ss = text.substring(0, cursorPos);
            if(ss.endsWith(Character.toString(STOP_TOKEN)))
                action = NULL;
            ss = ss.replaceAll(Character.toString(STOP_TOKEN), "");
            var split = ss.split("\n");
            if(split.length > 2 && ss.endsWith("\n")){
                action = ANIMATE_TEXT_SHIFT;
                shiftAnimation = 0;
                return;
            }
            String[] finalSplit;
            if(split.length > 3)
                finalSplit = Arrays.copyOfRange(split, split.length - 3, split.length);
            else
                finalSplit = split;
            for (int i = 0; i < finalSplit.length; i++) {
                switch (i) {
                    case 0 -> first_line.change(tc -> tc.setText(finalSplit[0]));
                    case 1 -> second_line.change(tc -> tc.setText(finalSplit[1]));
                    case 2 -> third_line.change(tc -> tc.setText(finalSplit[2]));
                }
            }
        }
    }

    private void updateArrow(){
        if(!visible)
            return;
        if(timer % 10 == 0)
            text_arrow.change(tc -> tc.setText(Character.toString((text_arrow.getText().charAt(0) - CURSOR_0 + 1) % 4 + CURSOR_0)));
    }

}
