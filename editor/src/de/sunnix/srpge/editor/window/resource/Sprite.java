package de.sunnix.srpge.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Sprite {

    public enum DirectionType {
        SINGLE, ALL_DIRECTIONS
    }

    public enum AnimationType {
        NONE, NORMAL, BOUNCE, SINGLE
    }

    @Setter
    private String image;
    @Getter
    private DirectionType directionType;
    @Getter
    @Setter
    private AnimationType animationType = AnimationType.NONE;
    private List<List<Integer>> animPattern;
    @Getter
    @Setter
    private int animationSpeed = 8;

    public Sprite(){
        setDirectionType(DirectionType.SINGLE);
    }

    public Sprite(DataSaveObject dso){
        this();
        load(dso);
    }

    public String getImageName(){
        return image;
    }

    public ImageResource getImage(Window window){
        return window.getSingleton(Resources.class).images.getData(image);
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

    public Collection<String> getDirectionsFromType(){
        return Arrays.stream((switch (directionType){
            case SINGLE -> new String[] { "Any" };
            default -> new String[] { "South", "West", "East", "North" };
        })).toList();
    }

    public List<Integer> getPattern(int direction){
        return animPattern.get(direction);
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

    public DataSaveObject save(DataSaveObject dso){
        if(image != null)
            dso.putString("image", image);
        if(directionType != DirectionType.SINGLE)
            dso.putByte("dir-type", (byte) directionType.ordinal());
        var patternDSO = new DataSaveObject();
        patternDSO.putInt("s", animPattern.size());
        for (int i = 0; i < animPattern.size(); i++)
            patternDSO.putList(Integer.toString(i), animPattern.get(i));
        dso.putObject("pattern", patternDSO);
        if(animationSpeed != 0)
            dso.putInt("animSpeed", animationSpeed);
        if(animationType != AnimationType.NONE)
            dso.putByte("anim-type", (byte) animationType.ordinal());
        return dso;
    }

}
