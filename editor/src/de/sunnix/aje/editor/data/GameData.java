package de.sunnix.aje.editor.data;

import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.util.LoadingDialog;
import de.sunnix.sdso.DataSaveObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class GameData {

    private final List<MapData> maps = new ArrayList<>();

    public String[] getMapListNames(){
        return maps.stream().map(MapData::toString).toArray(String[]::new);
    }

    public int addMap(MapData map){
        maps.add(map);
        maps.sort(Comparator.comparing(MapData::getID));
        return map.getID();
    }

    public int genNewMap() {
        return addMap(new MapData(genNextID(), MapData.MINIMUM_WIDTH, MapData.MINIMUM_HEIGHT));
    }

    public int genNextID() throws RuntimeException{
        var i = 0;
        for(; i < maps.size() && i < 10000; i++){
            if(maps.get(i).getID() > i)
                return i;
        }
        if(i == 10000)
            throw new RuntimeException("Maximum number of maps reached!");
        else
            return i;
    }

    public String getMapNameOf(int id) {
        var map = FunctionUtils.firstOrNull(maps, x -> x.getID() == id);
        return map == null ? null : map.toString();
    }

    public MapData getMap(int id) {
        return FunctionUtils.firstOrNull(maps, x -> x.getID() == id);
    }

    public void deleteMap(int id) {
        maps.removeIf(x -> x.getID() == id);
    }

    public void reset(){
        maps.clear();
    }

    public void saveData(LoadingDialog dialog, int progress, ZipOutputStream zip) throws IOException {
        saveMaps(dialog, progress, zip);
    }

    private void saveMaps(LoadingDialog dialog, int progress, ZipOutputStream zip) throws IOException {
        var resFolder = new File("maps");
        var conf = new DataSaveObject();
        conf.putList("config", maps.stream().map(x -> (short) x.getID()).toList());
        zip.putNextEntry(new ZipEntry(new File(resFolder, "config").getPath()));
        var oStream = new ByteArrayOutputStream();
        conf.save(oStream);
        zip.write(oStream.toByteArray());
        var progressPerMap = (int)((double) progress / maps.size());
        for (var map : maps) {
            zip.putNextEntry(new ZipEntry(new File(resFolder, String.format("%04d.map", map.getID())).getPath()));
            var dso = new DataSaveObject();
            map.saveMap(dso);
            oStream = new ByteArrayOutputStream();
            dso.save(oStream);
            zip.write(oStream.toByteArray());
            dialog.addProgress(progressPerMap);
        }
    }

    public void loadData(LoadingDialog dialog, int progress, ZipFile zip, int[] version) throws IOException {
        var resFolder = new File("maps");
        var stream = zip.getInputStream(new ZipEntry(new File(resFolder, "config").getPath()));
        var conf = new DataSaveObject().load(stream);
        stream.close();
        var mapList = conf.<Short>getList("config");
        var progressPerMap = (int)((double) progress / mapList.size());
        for(var mapID: mapList){
            stream = zip.getInputStream(new ZipEntry(new File(resFolder, String.format("%04d.map", mapID)).getPath()));
            var dso = new DataSaveObject().load(stream);
            maps.add(new MapData(dso, version));
            stream.close();
            dialog.addProgress(progressPerMap);
        }
    }

}
