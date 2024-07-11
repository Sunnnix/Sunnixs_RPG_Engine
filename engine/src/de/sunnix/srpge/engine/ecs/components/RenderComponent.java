package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.State;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.graphics.TextureRenderObject;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.resources.Sprite;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Objects;

public class RenderComponent extends Component{

    private Sprite currentSprite;
    private Sprite sprite;
    private HashMap<String, Sprite> stateSprites = new HashMap<>();

    private long animTimer;
    private int animPos = -1;

    @Setter
    private int direction;

    private TextureRenderObject renderObject;

    public RenderComponent(DataSaveObject dso){
        sprite = Resources.get().getSprite(dso.getString("sprite", null));
        currentSprite = sprite;
        dso.<DataSaveObject>getList("state-sprites")
                .forEach(x -> stateSprites.put(x.getString("state", null), Resources.get().getSprite(x.getString("sprite", null))));
    }

    @Override
    public void init(World world, GameObject parent) {
        super.init(world, parent);
        if(sprite == null)
            return;
        var tex = sprite.getTexture();
        renderObject = new TextureRenderObject(tex);
        renderObject.getSize().set((float) tex.getWidth() / tex.getTileWidth(), (float) tex.getHeight() / tex.getTileHeight());
        RenderSystem.addGO(parent);
    }

    public void render(GameObject go) {
        if(!isValid() || currentSprite == null)
            return;
        if(go.isStatesChanged())
            getNextPrioSprite(go);
        if(currentSprite == null)
            return;
        animTimer++;
        var index = currentSprite.getTextureIndexForAnimation(animTimer, direction);
        if(index == -1)
            return;
        if(index != animPos){
            animPos = index;
            var texture = currentSprite.getTexture();
            if(texture != null){
                var texPos = texture.getTexturePositions(index);
                renderObject.getMesh().changeBuffer(1, texPos);
            }
        }

        renderObject.render(go.getPosition().mul(1, -1, 1, new Vector3f()), go.size.x, go.getZ_pos());
    }

    private void getNextPrioSprite(GameObject go) {
        if(renderObject == null)
            return;
        var states = go.getStates().stream().filter(x -> stateSprites.containsKey(x.id())).toList();
        var curPrio = -1;
        State state = null;
        for(var s : states){
            if(s.priority() <= curPrio)
                continue;
            curPrio = s.priority();
            state = s;
        }
        Sprite nextSprite = null;
        if(state != null)
            nextSprite = stateSprites.get(state.id());
        if(nextSprite == null)
            nextSprite = sprite;
        if(Objects.equals(currentSprite, nextSprite))
            return;
        currentSprite = nextSprite;
        var tex = currentSprite.getTexture();
        if(tex == null)
            return;
        renderObject.setTexture(tex);
        renderObject.getSize().set((float) tex.getWidth() / tex.getTileWidth(), (float) tex.getHeight() / tex.getTileHeight());
        animTimer = 0;
        animPos = -1;
    }

    @Override
    public boolean isValid() {
        return renderObject != null && renderObject.isValid();
    }
}
