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
    public Runnable createView(Window window, GameObject object, JPanel parent) {
        var setSpriteBtn = addView(parent, new JButton("Set Sprite"));
        var spriteView = addView(parent, new JPanel(){

            long timer;
            int currentSpriteIndex = -1;

            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(currentSpriteIndex == -1 || sprite == null || sprite.isBlank())
                    return;

                var sprite = window.getSingleton(Resources.class).sprites.getData(RenderComponent.this.sprite);
                if(sprite == null)
                    return;

                var texture = sprite.getImage(window);
                if(texture == null)
                    return;
                var image = texture.getImage();
                if(image == null)
                    return;
                var aWidth = texture.getWidth();
                var aHeight = texture.getHeight();

                currentSpriteIndex %= aWidth * aHeight;

                var spriteWidth = image.getWidth() / aWidth;
                var spriteHeight = image.getHeight() / aHeight;

                var x = getWidth() / 2 - spriteWidth / 2;
                var y = getHeight() / 2 - spriteHeight / 2;

                var srcX1 = (currentSpriteIndex % aWidth) * spriteWidth;
                var srcY1 = (currentSpriteIndex / aWidth % aHeight) * spriteHeight;
                var srcX2 = srcX1 + spriteWidth;
                var srcY2 = srcY1 + spriteHeight;

                g.drawImage(image, x, y, x + spriteWidth, y + spriteHeight, srcX1, srcY1, srcX2, srcY2, null);
            }
        }, 250);

        setSpriteBtn.addActionListener(a -> {
            var newSprite = window.getSingleton(Resources.class).sprites.showSelectDialogSinglePath(parent, "Select sprite", null, "Sprite", sprite);
            if(newSprite == null)
                return;
            sprite = newSprite;
            spriteView.timer = 0;
            parent.repaint();
        });
        return () -> {
            if(RenderComponent.this.sprite == null || RenderComponent.this.sprite.isBlank())
                return;
            var sprite = window.getSingleton(Resources.class).sprites.getData(RenderComponent.this.sprite);
            if(sprite == null)
                return;
            spriteView.timer++;
            var index = sprite.getTextureIndexForAnimation(spriteView.timer, 0);
            if(index != spriteView.currentSpriteIndex){
                spriteView.currentSpriteIndex = index;
                parent.repaint();
            }
        };
    }

    @Override
    public void onDraw(Window window, Graphics2D g, float zoom, int x, int y, int w, int h, int d, boolean selected) {
        var sprite = window.getSingleton(Resources.class).sprites.getData(this.sprite);
        if(sprite == null)
            return;
        sprite.drawSprite(window, g, 0, 0, zoom, x, y);
    }
}
