package de.sunnix.srpge.engine.graphics.gui;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.GlobalConfig;
import de.sunnix.srpge.engine.InputManager;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.audio.AudioResource;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.gui.text.Text;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

import static de.sunnix.srpge.engine.graphics.gui.SpeechBox.State.*;
import static de.sunnix.srpge.engine.graphics.gui.text.Text.*;

public class SpeechBox {

    enum State {
        NONE, FADE_IN, FADE_OUT, ANIMATE_TEXT, ANIMATE_TEXT_SHIFT, FADE_IN_YES_NO, FADE_OUT_YES_NO
    }
    private static final float[] textColor = { .15f, .17f, .1f, 1f };
    @Getter
    private int id;
    private String name, text;
    private final TextBoxRenderObject scroll = new TextBoxRenderObject();
    private final NameBoxRenderObject nameBoxSmall = new NameBoxRenderObject(scroll, 0);
    private final NameBoxRenderObject nameBoxMedium = new NameBoxRenderObject(scroll, 1);
    private final NameBoxRenderObject nameBoxLarge = new NameBoxRenderObject(scroll, 2);
    private NameBoxRenderObject currentNameBox = nameBoxSmall;
    private final NameBoxRenderObject yesNoBox = new NameBoxRenderObject(scroll, 0);
    private final Text first_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text second_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text third_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text text_arrow = new Text(false, tc -> tc.setText(Character.toString(CURSOR_0)).setExceptionalColoringChars(CURSOR_0, CURSOR_3 + 1)).setSize(10);
    private final Text question_arrow = new Text(false, tc -> tc.setText(Character.toString(CURSOR_0)).setExceptionalColoringChars(CURSOR_0, CURSOR_3 + 1)).setSize(10);
    private final Text name_line = new Text(false, tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);
    private final Text yes_no_line = new Text(false, tc -> tc.setText("YES    NO").setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setExceptionalColoringChars(ARROW_RIGHT, CURSOR_3 + 1).setDrawShadow(false)).setSize(10);

    @Getter
    private boolean visible;
    @Getter
    private boolean finished = true;
    private State state;

    private final float maxYShift = scroll.getSize().y * 1.5f;
    private final float scrollSpeedY = maxYShift / 20;
    private final float scrollSpeedX = Camera.getSize().x / 20;
    private final float maxXShiftOptions = yesNoBox.getSize().x * 2;
    private final float scrollSpeedXOptions = Camera.getSize().x / 30;
    private int timer;

    private int cursorPos;
    private int selectedOption; // 0 = Yes, 1 = No

    public enum SoundType{
        NONE, MALE, FEMALE, CHILD, EVIL
    }

    private SoundType soundType = SoundType.NONE;
    private Consumer<Boolean> onYesNo;

    private final AudioResource open;
    private final AudioResource close;
    private final AudioResource male;
    private final AudioResource female;
    private final AudioResource child;
    private final AudioResource evil;
    private final AudioResource option_cursor;
    private final AudioResource option_open;
    private final AudioResource option_close;
    private final AudioResource option_confirm;
    private final AudioResource option_cancel;

    public SpeechBox() {
        try {
            open = createAR("open", "/data/sounds/Text_open.wav");
            close = createAR("close", "/data/sounds/Text_close.wav");
            male = createAR("male", "/data/sounds/Text_male.wav");
            female = createAR("female", "/data/sounds/Text_female.wav");
            child = createAR("child", "/data/sounds/Text_child.wav");
            evil = createAR("evil", "/data/sounds/Text_evil.wav");
            option_cursor = createAR("evil", "/data/sounds/Cursor.wav");
            option_open = createAR("evil", "/data/sounds/Menu_open.wav");
            option_close = createAR("evil", "/data/sounds/Menu_close.wav");
            option_confirm = createAR("evil", "/data/sounds/Confirm.wav");
            option_cancel = createAR("evil", "/data/sounds/Cancel.wav");
        } catch (Exception e){
            throw new RuntimeException("Error loading Soundfiles for SpeechBox", e);
        }

        first_line.setPos(26, 182 + scroll.getOffsetY());
        second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight());
        third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
        text_arrow.setPos(Camera.getSize().x - 30, third_line.getPos().y - 10 - scroll.getOffsetY());
        yesNoBox.getPos().set(320, 65);
    }

    private static AudioResource createAR(String name, String path) throws Exception {
        return new AudioResource(new DataSaveObject() {
            {
                InputStream stream;
                putString("name", name);
                putFloat("def_gain", 4);
                putArray("data", (stream = new BufferedInputStream(getClass().getResourceAsStream(path))).readAllBytes());
                putString("extension", "wav");
                stream.close();
            }
        });
    }

    public void render(){
        if(visible) {
            timer++;
            scroll.render();
            if(name != null){
                currentNameBox.render();
                name_line.setPos(currentNameBox.getPos().x + currentNameBox.getSize().x / 2 - name_line.getWidth() / 2 + currentNameBox.getOffsetX(), currentNameBox.getPos().y + 87);
                name_line.render();
            }
            if(state != FADE_IN)
                renderText();
            if(onYesNo != null && finished){
                yesNoBox.render();
                yes_no_line.setPos(yesNoBox.getPos().x + yesNoBox.getSize().x / 2 - yes_no_line.getWidth() / 2 + yesNoBox.getOffsetX(),  Camera.getSize().y - yesNoBox.getPos().y - 23);
                yes_no_line.render();
                question_arrow.setPos(yes_no_line.getPos().x + 4 + (selectedOption * 32), yes_no_line.getPos().y - 12);
                question_arrow.render();
            }
        } else
            timer = 0;
        update();
    }

    public void showText(int id, String name, String text, SoundType soundType, Consumer<Boolean> onYesNo){
        if(this.visible)
            throw new IllegalStateException("Can't create new text when speech box is showing");
        this.onYesNo = onYesNo;
        this.selectedOption = 0;
        this.soundType = soundType;
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

        this.state = FADE_IN;
        AudioManager.get().playSound(open);
    }

    private void renderText(){
        if(!first_line.getText().isEmpty())
            first_line.render();
        if(!second_line.getText().isEmpty())
            second_line.render();
        if(!third_line.getText().isEmpty())
            third_line.render();
        if(state == NONE && !finished && onYesNo == null)
            text_arrow.render();
    }

    private void update(){
        switch (state){
            case FADE_IN -> updateFadeIn();
            case FADE_OUT -> updateFadeOut();
            case ANIMATE_TEXT, ANIMATE_TEXT_SHIFT -> updateText();
            case FADE_IN_YES_NO -> updateYesNoFadeIn();
            case FADE_OUT_YES_NO -> updateYesNoFadeOut();
            case NONE -> {
                if(finished)
                    if(InputManager.PAD_LEFT.startPressed()) {
                        selectedOption = 0;
                        AudioManager.get().playSound(option_cursor);
                    } else if(InputManager.PAD_RIGHT.startPressed()) {
                        selectedOption = 1;
                        AudioManager.get().playSound(option_cursor);
                    }
                updateArrow();
                if(finished) {
                    if(onYesNo != null) {
                        if(InputManager.PAD_A.startPressed()) {
                            onYesNo.accept(selectedOption == 0);
                            AudioManager.get().playSound(selectedOption == 0 ? option_confirm : option_cancel);
                            AudioManager.get().playSound(option_close);
                            state = FADE_OUT_YES_NO;
                        }
                    } else if (InputManager.PAD_X.startPressed()) {
                        AudioManager.get().playSound(close);
                        state = FADE_OUT;
                    }
                }
                else if(InputManager.PAD_X.startPressed())
                    state = ANIMATE_TEXT;
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
            state = ANIMATE_TEXT;
    }

    private void updateFadeOut() {
        scroll.setOffsetY(scroll.getOffsetY() + scrollSpeedY);
        if(name != null)
            currentNameBox.setOffsetX(currentNameBox.getOffsetX() + scrollSpeedX);
        first_line.getPos().add(0, scrollSpeedY);
        second_line.getPos().add(0, scrollSpeedY);
        third_line.getPos().add(0, scrollSpeedY);
        if(scroll.getOffsetY() >= maxYShift) {
            state = NONE;
            first_line.change(tc -> tc.setText(""));
            second_line.change(tc -> tc.setText(""));
            third_line.change(tc -> tc.setText(""));
            visible = false;
        }
    }

    private void updateYesNoFadeIn(){
        yesNoBox.setOffsetX(yesNoBox.getOffsetX() - scrollSpeedXOptions);
        if(yesNoBox.getOffsetX() <= 0)
            state = NONE;
    }

    private void updateYesNoFadeOut(){
        yesNoBox.setOffsetX(yesNoBox.getOffsetX() + scrollSpeedXOptions);
        if(yesNoBox.getOffsetX() >= maxXShiftOptions){
            AudioManager.get().playSound(close);
            state = FADE_OUT;
        }
    }

    private int shiftAnimation = 0;

    private void updateText(){
        var ignoreCheck = false;
        if(state == ANIMATE_TEXT_SHIFT){
            var maxShiftAnim = 20;
            shiftAnimation += ((InputManager.PAD_X.isPressed()) ? 3 : 1);
            var animProgress = 1f / maxShiftAnim * shiftAnimation;
            if(shiftAnimation < maxShiftAnim) {
                first_line.change(tc -> tc.setColor(textColor[0], textColor[1], textColor[2], 1 - animProgress * 2));
                second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight() * (1 - animProgress));
                third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
                return;
            } else {
                state = ANIMATE_TEXT;
                first_line.change(tc -> tc.setColor(textColor[0], textColor[1], textColor[2], textColor[3]).setText(second_line.getText()));
                second_line.setPos(first_line.getPos().x, first_line.getPos().y + first_line.getHeight());
                second_line.change(tc -> tc.setText(third_line.getText()));
                third_line.setPos(second_line.getPos().x, second_line.getPos().y + second_line.getHeight());
                third_line.change(tc -> tc.setText(""));
            }
        }
        if(timer % ((InputManager.PAD_X.isPressed()) ? 4 : 7) == 0)
            switch (soundType){
                case MALE -> AudioManager.get().playSound(male);
                case FEMALE -> AudioManager.get().playSound(female);
                case CHILD -> AudioManager.get().playSound(child);
                case EVIL -> AudioManager.get().playSound(evil);
            }

        if(!ignoreCheck && (!(InputManager.PAD_X.isPressed()) && timer % 4 != 0))
            return;
        cursorPos++;
        if(cursorPos == text.length() + 1) {
            finished = true;
            if(onYesNo != null) {
                state = FADE_IN_YES_NO;
                AudioManager.get().playSound(option_open);
            } else
                state = NONE;
        } else {
            var ss = text.substring(0, cursorPos);
            if(ss.endsWith(Character.toString(STOP_TOKEN)))
                state = NONE;
            ss = ss.replaceAll(Character.toString(STOP_TOKEN), "");
            var split = ss.split("\n");
            if(split.length > 2 && ss.endsWith("\n")){
                state = ANIMATE_TEXT_SHIFT;
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
            if(!finished && onYesNo == null)
                text_arrow.change(tc -> tc.setText(Character.toString((text_arrow.getText().charAt(0) - CURSOR_0 + 1) % 4 + CURSOR_0)));
            else
                question_arrow.change(tc -> tc.setText(Character.toString((question_arrow.getText().charAt(0) - CURSOR_0 + 1) % 4 + CURSOR_0)));
    }

}
