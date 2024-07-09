package de.sunnix.srpge.engine.resources;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.graphics.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class Sprite {

    public enum DirectionType {
        SINGLE, ALL_DIRECTIONS
    }

    public enum AnimationType {
        NONE, NORMAL, BOUNCE, SINGLE
    }

    private String image;
    private DirectionType directionType;
    private AnimationType animationType = AnimationType.NONE;
    private List<List<Integer>> animPattern;
    private int animationSpeed = 8;

    public Sprite(DataSaveObject dso){
        load(dso);
    }

    public void setDirectionType(DirectionType directionType) {
        this.directionType = directionType;
        switch (directionType){
            case SINGLE -> {
                if(animPattern == null){
                    animPattern = new ArrayList<>();
                    animPattern.add(new ArrayList<>());
                }
                while (animPattern.size() > 1)
                    animPattern.remove(1);
            }
            default -> {
                if(animPattern == null)
                    animPattern = new ArrayList<>();
                while (animPattern.size() < 4)
                    animPattern.add(new ArrayList<>());
            }
        }
    }

    public int getTextureIndexForAnimation(long timer, int direction){
        if(direction == -1)
            return -1;
        var pattern = animPattern.get(direction % animPattern.size());
        if(pattern.isEmpty())
            return -1;
        var patternIndex = timer / animationSpeed;

        patternIndex = switch (animationType){
            case NONE -> 0;
            case NORMAL -> patternIndex % pattern.size();
            case SINGLE -> patternIndex < pattern.size() - 1 ? patternIndex : pattern.size() - 1;
            case BOUNCE -> {
                var index = patternIndex % Math.max(1, pattern.size() * 2 - 2);
                if(index >= pattern.size())
                    yield pattern.size() - 1 - index % pattern.size() - 1;
                else
                    yield index;
            }
        };

        return pattern.get((int) patternIndex);
    }

    public void load(DataSaveObject dso){
        image = dso.getString("image", null);
        setDirectionType(DirectionType.values()[dso.getByte("dir-type", (byte) 0) % DirectionType.values().length]);
        animationSpeed = dso.getInt("animSpeed", 8);
        var patternDSO = dso.getObject("pattern");
        if(patternDSO != null)
            for (int i = 0; i < Math.min(animPattern.size(), patternDSO.getInt("s", 0)); i++)
                animPattern.get(i).addAll(patternDSO.getList(Integer.toString(i)));
        animationType = AnimationType.values()[dso.getByte("anim-type", (byte) 0)];
    }

    public TextureAtlas getTexture(){
        return Resources.get().getTexture(image);
    }

}
