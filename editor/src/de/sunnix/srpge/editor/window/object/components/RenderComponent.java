package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class RenderComponent extends Component{

    private String sprite;

    public RenderComponent() {
        super("render");
    }

    @Override
    public String genName() {
        return "Renderer";
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        sprite = dso.getString("sprite", null);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("sprite", sprite);
        return dso;
    }

    @Override
    public void createView(Window window, GameObject object, JPanel parent) {
        var setSpriteBtn = addView(parent, new JButton("Set Sprite"));
        addView(parent, new JPanel(){

            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(sprite == null || sprite.isBlank())
                    return;
                var res = window.getSingleton(Resources.class);
                var image = res.image_getRaw(sprite);
                if(image == null)
                    return;
                g.drawImage(image, getWidth() / 2 - image.getWidth() / 2, getHeight() / 2 - image.getHeight() / 2, null);
            }
        }, 250);

        setSpriteBtn.addActionListener(a -> {
            var newSprite = window.getSingleton(Resources.class).images.showSelectDialogSinglePath(parent, "Select sprite", null, "Sprite", sprite);
            if(newSprite == null)
                return;
            sprite = newSprite;
            parent.repaint();
        });
    }

}
