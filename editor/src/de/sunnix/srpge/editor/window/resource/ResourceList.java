package de.sunnix.srpge.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.util.LoadingDialog;
import de.sunnix.srpge.editor.window.customswing.DefaultValueComboboxModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static de.sunnix.srpge.editor.lang.Language.getString;

/**
 * A class to manage categorized resources of type T. Resources are stored in a hierarchical structure
 * where each resource belongs to a category.
 *
 * @param <T> The type of the resources managed by this class.
 */
public class ResourceList<T> {

    /**
     * The name of the resource list, used as the base folder name for storing its data.
     */
    public final String name;

    /**
     * A consumer that defines actions to be performed when a resource is removed.
     */
    private final Consumer<T> onRemoving;

    /**
     * A map storing the resources, categorized by their respective categories.
     */
    private final Map<String, Map<String, T>> resources = new HashMap<>();

    /**
     * Constructs a ResourceList with a specified name and an onRemoving action.
     *
     * @param name       The name of the resource list. It should be unique as it is used to create a folder for its data.
     * @param onRemoving A consumer that defines actions to be performed when a resource is removed. Pass null if not needed.
     */
    public ResourceList(String name, Consumer<T> onRemoving){
        this.name = name;
        this.onRemoving = onRemoving;
    }

    /**
     * Constructs a ResourceList with a specified name.
     *
     * @param name The name of the resource list. It should be unique as it is used to create a folder for its data.
     */
    public ResourceList(String name){
        this(name, null);
    }

    /**
     * Resets the resource list by clearing all resources. If an onRemoving action is defined, it is applied to each resource before removal.
     */
    public void reset(){
        if(onRemoving != null)
            resources.values().forEach(entry -> entry.values().forEach(onRemoving));
        resources.clear();
    }

    /**
     * Gets the names of all categories in the resource list.
     *
     * @return A collection of category names.
     */
    public Collection<String> getCategoryNames(){
        return resources.keySet();
    }

    /**
     * Gets the names of all resources in a specified category.
     *
     * @param category The category name.
     * @return A collection of resource names in the specified category.
     */
    public Collection<String> getDataNames(String category){
        return resources.getOrDefault(category, Collections.emptyMap()).keySet();
    }

    /**
     * Retrieves a resource by its full name (category/name).
     *
     * @param name The full name of the resource.
     * @return The resource, or null if not found.
     */
    public T getData(String name){
        if(name == null)
            return null;
        var split = name.split("/");
        if(split.length < 2)
            return null;
        return getData(split[0], split[1]);
    }

    /**
     * Retrieves a resource by its category and name.
     *
     * @param category The category name.
     * @param name     The resource name.
     * @return The resource, or null if not found.
     */
    public T getData(String category, String name){
        if(category == null || name == null)
            return null;
        var cat = resources.get(category);
        if(cat == null)
            return null;
        return cat.get(name);
    }

    /**
     * Gets all resources in a specified category.
     *
     * @param category The category name.
     * @return A collection of resources in the specified category.
     */
    public Collection<T> getAllData(String category){
        return resources.getOrDefault(category, Collections.emptyMap()).values();
    }

    /**
     * Adds a new category to the resource list.
     *
     * @param name The name of the category.
     * @throws NullPointerException If the category name is null.
     */
    public void addCategory(String name) throws NullPointerException{
        if(name == null)
            throw new NullPointerException("name can't be null");
        resources.put(name, new HashMap<>());
    }

    /**
     * Adds a resource to the resource list using a full name (category/name).
     *
     * @param name The full name of the resource.
     * @param data The resource data.
     * @return The added resource.
     * @throws NullPointerException If the name is null.
     * @throws IllegalArgumentException If the name is invalid.
     */
    public T addData(String name, T data) throws NullPointerException, IllegalArgumentException {
        if(name == null)
            throw new NullPointerException("name can't be null");
        var split = name.split("/");
        if(split.length < 2)
            throw new IllegalArgumentException("name is invalid \"" + name + "\"");
        return addData(split[0], split[1], data);
    }

    /**
     * Adds a resource to the resource list using a category and a name.
     *
     * @param category The category name.
     * @param name     The resource name.
     * @param data     The resource data.
     * @return The added resource.
     * @throws RuntimeException If the category or name is null.
     */
    public T addData(String category, String name, T data) throws NullPointerException{
        if(category == null)
            throw new NullPointerException("category can't be null");
        if(name == null)
            throw new NullPointerException("name can't be null");
        resources.computeIfAbsent(category, k -> new HashMap<>()).put(name, data);
        return data;
    }

    /**
     * Removes a category from the resource list. If an onRemoving action is defined, it is applied to each resource in the category before removal.
     *
     * @param category The category name.
     */
    public void removeCategory(String category) {
        if(onRemoving != null)
            resources.getOrDefault(category, Collections.emptyMap()).values().forEach(onRemoving);
        resources.remove(category);
    }

    /**
     * Removes a resource from the resource list using its full name (category/name).
     *
     * @param name The full name of the resource.
     * @return The removed resource, or null if not found.
     */
    public T removeData(String name){
        if(name == null)
            return null;
        var split = name.split("/");
        if(split.length < 2)
            return null;
        return removeData(split[0], split[1]);
    }

    /**
     * Removes a resource from the resource list using its category and name.
     *
     * @param category The category name.
     * @param name     The resource name.
     * @return The removed resource, or null if not found.
     */
    public T removeData(String category, String name){
        if(category == null || name == null)
            return null;
        var cat = resources.get(category);
        if(cat == null)
            return null;
        var data = cat.remove(name);
        if(data != null && onRemoving != null)
            onRemoving.accept(data);
        return data;
    }

    /**
     * Renames a category in the resource list.
     *
     * @param nameBefore The current category name.
     * @param nameAfter  The new category name.
     */
    public void renameCategory(String nameBefore, String nameAfter){
        var cat = resources.remove(nameBefore);
        if(cat != null)
            resources.put(nameAfter, cat);
    }

    /**
     * Renames a resource within a category.
     *
     * @param category  The category name.
     * @param nameBefore The current resource name.
     * @param nameAfter  The new resource name.
     * @return The renamed resource, or null if not found.
     */
    public T renameData(String category, String nameBefore, String nameAfter){
        var cat = resources.get(category);
        if(cat == null)
            return null;
        var data = cat.remove(nameBefore);
        if (data == null)
            return null;
        cat.put(nameAfter, data);
        return data;
    }

    /**
     * Moves a resource from one category to another.
     *
     * @param nameBefore The current category name.
     * @param nameAfter  The new category name.
     * @param dataName   The resource name.
     */
    public void moveData(String nameBefore, String nameAfter, String dataName){
        var cat = resources.get(nameBefore);
        if(cat == null)
            return;
        var data = cat.remove(dataName);
        if(data == null)
            return;
        resources.computeIfAbsent(nameAfter, k -> new HashMap<>()).put(dataName, data);
    }

    /**
     * Checks if the resource list contains a specified category.
     *
     * @param category The category name.
     * @return true if the category exists, false otherwise.
     */
    public boolean containsCategory(String category){
        return resources.containsKey(category);
    }

    /**
     * Checks if the resource list contains a resource by its full name (category/name).
     *
     * @param name The full name of the resource.
     * @return true if the resource exists, false otherwise.
     */
    public boolean containsDataName(String name){
        if(name == null)
            return false;
        var split = name.split("/");
        if(split.length < 2)
            return false;
        return containsDataName(split[0], split[1]);
    }

    /**
     * Checks if the resource list contains a resource by its category and name.
     *
     * @param category The category name.
     * @param name     The resource name.
     * @return true if the resource exists, false otherwise.
     */
    public boolean containsDataName(String category, String name){
        if(category == null)
            return false;
        if(name == null)
            return false;
        var cat = resources.get(category);
        if(cat == null)
            return false;
        return cat.containsKey(name);
    }

    /**
     * Loads resources from a zip file.
     *
     * @param dialog       A loading dialog to display progress.
     * @param progress     The total progress that the function should have added to the dialog after the function is finished
     * @param zip          The zip file containing the resources.
     * @param generateData A function to generate resource objects from data save objects.
     */
    public void load(LoadingDialog dialog, int progress, ZipFile zip, Function<DataSaveObject, T> generateData) {
        reset();
        var subFolder = new File("res", name);
        try{
            var entries = zip.entries();
            var files = new ArrayList<ZipEntry>();
            while(entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(subFolder.getPath()))
                    continue;
                files.add(e);
            }
            var progressPerFile = (int)((double) progress / files.size());
            for(var e: files){
                var category = resources.computeIfAbsent(e.getName().substring(subFolder.getPath().length() + 1), k -> new HashMap<>());
                var dsos = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList(name);
                for (DataSaveObject dso : dsos) {
                    var name = dso.getString("name", null);
                    if(name == null)
                        continue;
                    var data = generateData.apply(dso);
                    if(data == null) {
                        System.err.println("Error on loading data " + name + " from " + this.name + " resources");
                        continue;
                    }
                    category.put(name, data);
                }
                dialog.addProgress(progressPerFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves resources to a zip output stream.
     *
     * @param dialog    A loading dialog to display progress.
     * @param progress  The total progress that the function should have added to the dialog after the function is finished
     * @param zip       The zip output stream to save the resources to.
     * @param saveData  A bi-consumer to save resource objects to data save objects.
     */
    public void save(LoadingDialog dialog, int progress, ZipOutputStream zip, BiConsumer<DataSaveObject, T> saveData){
        var imgFolder = new File("res", name);
        try{
            var progressPerFile = (int)((double) progress / resources.size());
            for(var category : resources.entrySet()){
                var dsos = new DataSaveObject();
                var list = new ArrayList<DataSaveObject>();
                for(var entry: category.getValue().entrySet()){
                    var dso = new DataSaveObject();
                    dso.putString("name", entry.getKey());
                    saveData.accept(dso, entry.getValue());
                    list.add(dso);
                }
                dsos.putList(name, list);
                zip.putNextEntry(new ZipEntry(new File(imgFolder, category.getKey()).getPath()));
                var oStream = new ByteArrayOutputStream();
                dsos.save(oStream);
                zip.write(oStream.toByteArray());
                oStream.close();
                dialog.addProgress(progressPerFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns an array of two JComboBox components for selecting categories and data names.<br>
     *
     * @param withDefaultValue determines if the first one (for the categories) contains a default none value for selection of no category
     * @return An array of two JComboBox components where the first JComboBox contains category names
     *         and the second JComboBox contains data names within the selected category.
     */
    public JComboBox<String>[] getSelectionBoxes(boolean withDefaultValue){
        JComboBox<String> categories;
        if(withDefaultValue)
            categories = new JComboBox<>(new DefaultValueComboboxModel<>(getString("name.none"), resources.keySet().toArray(String[]::new)));
        else
            categories = new JComboBox<>(resources.keySet().toArray(String[]::new));
        var data = new JComboBox<String>(new DefaultComboBoxModel<>());
        categories.addActionListener(l -> {
            data.removeAllItems();
            if(categories.getSelectedIndex() == 0)
                return;
            ((DefaultComboBoxModel<String>)data.getModel()).addAll(getDataNames((String)categories.getSelectedItem()));
        });
        return new JComboBox[] { categories, data };
    }

    /**
     * Creates and returns an array of two JComboBox components for selecting categories and data names.<br>
     * The first one (for the categories) contains a default none value for selection of no category
     *
     * @return An array of two JComboBox components where the first JComboBox contains category names
     *         and the second JComboBox contains data names within the selected category.
     */
    public JComboBox<String>[] getSelectionBoxes(){
        return getSelectionBoxes(true);
    }

    /**
     * Shows a selection dialog with two JComboBox components for selecting a category and a data name,
     * with optional default selections.
     *
     * @param parent          The parent component of the dialog.
     * @param title           The title of the dialog.
     * @param message         The message displayed in the dialog.
     * @param dataDisplayName The display name for the data in the dialog.
     * @param defaultCategory The default category to be selected when the dialog is shown, or null for no default selection.
     * @param defaultData     The default data name to be selected when the dialog is shown, or null for no default selection.
     * @return An array of two strings where the first element is the selected category and the second element is the selected data name.
     *         Returns null if the dialog is canceled.
     */
    public String[] showSelectDialog(JComponent parent, String title, String message, String dataDisplayName, String defaultCategory, String defaultData){
        var boxes = getSelectionBoxes();
        if(defaultCategory != null) {
            boxes[0].setSelectedItem(defaultCategory);
            if (defaultData != null)
                boxes[1].setSelectedItem(defaultData);
        }
        if(!DialogUtils.showMultiInputDialog(parent, title, message, new String[] { getString("name.category"), dataDisplayName }, new JComponent[]{ boxes[0], boxes[1] }))
            return null;
        return new String[] { (String) boxes[0].getSelectedItem(), (String) boxes[1].getSelectedItem() };
    }

    /**
     * Shows a selection dialog with two JComboBox components for selecting a category and a data name,
     * with optional default selections. If a single string for default data is provided, it splits
     * the string into category and data name using "/" as the delimiter.
     *
     * @param parent          The parent component of the dialog.
     * @param title           The title of the dialog.
     * @param message         The message displayed in the dialog.
     * @param dataDisplayName The display name for the data in the dialog.
     * @param defaultData     The default data name to be selected when the dialog is shown, or null for no default selection.
     *                        The string should be in the format "category/dataName".
     * @return An array of two strings where the first element is the selected category and the second element is the selected data name.
     *         Returns null if the dialog is canceled.
     */
    public String[] showSelectDialog(JComponent parent, String title, String message, String dataDisplayName, String defaultData){
        String category = null;
        String data = null;
        if(defaultData != null){
            var split = defaultData.split("/");
            if(split.length == 2) {
                category = split[0];
                data = split[1];
            }
        }
        return showSelectDialog(parent, title, message, dataDisplayName, category, data);
    }

    /**
     * Shows a selection dialog with two JComboBox components and returns the selected category and data name
     * as a single path string, with optional default selections.
     *
     * @param parent          The parent component of the dialog.
     * @param title           The title of the dialog.
     * @param message         The message displayed in the dialog.
     * @param dataDisplayName The display name for the data in the dialog.
     * @param defaultCategory The default category to be selected when the dialog is shown, or null for no default selection.
     * @param defaultData     The default data name to be selected when the dialog is shown, or null for no default selection.
     * @return A string representing the selected category and data name in the format "category/dataName".
     *         Returns an empty string if either the category or data name is blank.
     *         Returns null if the dialog is canceled.
     */
    public String showSelectDialogSinglePath(JComponent parent, String title, String message, String dataDisplayName, String defaultCategory, String defaultData) {
        var names = showSelectDialog(parent, title, message, dataDisplayName, defaultCategory, defaultData);
        if(names == null)
            return null;
        if(names[0] == null || names[0].isBlank() || names[1] == null || names[1].isBlank())
            return "";
        return names[0] + "/" + names[1];
    }

    /**
     * Shows a selection dialog with two JComboBox components and returns the selected category and data name
     * as a single path string, with optional default selections. If a single string for default data is provided,
     * it splits the string into category and data name using "/" as the delimiter.
     *
     * @param parent          The parent component of the dialog.
     * @param title           The title of the dialog.
     * @param message         The message displayed in the dialog.
     * @param dataDisplayName The display name for the data in the dialog.
     * @param defaultData     The default data name to be selected when the dialog is shown, or null for no default selection.
     *                        The string should be in the format "category/dataName".
     * @return A string representing the selected category and data name in the format "category/dataName".
     *         Returns an empty string if either the category or data name is blank.
     *         Returns null if the dialog is canceled.
     */
    public String showSelectDialogSinglePath(JComponent parent, String title, String message, String dataDisplayName, String defaultData) {
        String category = null;
        String data = null;
        if(defaultData != null){
            var split = defaultData.split("/");
            if(split.length == 2) {
                category = split[0];
                data = split[1];
            }
        }
        return showSelectDialogSinglePath(parent, title, message, dataDisplayName, category, data);
    }

    /**
     * Creates a builder for building two JLists to access and modify the data of these resources
     * @param dataGenerator a generator for creating new data
     * @return the builder
     */
    public JListBuilder getJListBuilder(Supplier<T> dataGenerator){
        return new JListBuilder(dataGenerator);
    }

    /**
     * A builder class for creating and configuring JList components for categories and data.
     */
    public final class JListBuilder {

        private Supplier<T> dataGenerator;
        private DefaultListModel<String> categoryModel, dataModel;
        private JList<String> categoryList, dataList;
        private JComponent parent;
        private String dataName = getString("name.data");
        private Runnable onChanged = () -> {};

        /**
         * Constructs a JListBuilder with the specified data generator.
         *
         * @param dataGenerator A Supplier that generates data of type T.
         */
        public JListBuilder(Supplier<T> dataGenerator){
            this.dataGenerator = dataGenerator;
        }

        /**
         * Sets the parent component for this JListBuilder.<br>
         * All Dialogs that open will align to the parent.
         *
         * @param parent The parent JComponent.
         * @return The current JListBuilder instance for method chaining.
         */
        public JListBuilder setParent(JComponent parent){
            this.parent = parent;
            return this;
        }

        /**
         * Sets the data type name used in dialogs and other UI components.
         *
         * @param dataName The name of the data type.
         * @return The current JListBuilder instance for method chaining.
         */
        public JListBuilder setDataName(String dataName) {
            this.dataName = dataName;
            return this;
        }

        /**
         * Sets a Runnable to be executed when changes occur.
         *
         * @param onChanged The Runnable to be executed on change.
         * @return The current JListBuilder instance for method chaining.
         */
        public JListBuilder setOnChange(Runnable onChanged){
            this.onChanged = onChanged;
            return this;
        }

        /**
         * Builds and returns an array containing the category and data JLists.
         *
         * @return An array containing the category JList at index 0 and the data JList at index 1.
         */
        public JList<String>[] build(){
            categoryList = new JList<>(categoryModel = new DefaultListModel<>());
            dataList = new JList<>(dataModel = new DefaultListModel<>());

            categoryModel.addAll(getCategoryNames());

            categoryList.addListSelectionListener(l -> {
                dataModel.clear();
                var selectedCat = categoryList.getSelectedValue();
                if(selectedCat == null)
                    return;
                dataModel.addAll(getDataNames(selectedCat));
            });

            categoryList.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() != MouseEvent.BUTTON3)
                        return;
                    new JPopupMenu(){
                        {
                            var create = new JMenuItem(getString("name.create"));
                            create.addActionListener(this::createCategory);
                            add(create);
                            if(categoryList.getSelectedIndex() != -1){
                                var change = new JMenuItem(getString("name.change"));
                                change.addActionListener(this::changeCategory);
                                add(change);
                                var remove = new JMenuItem(getString("name.remove"));
                                remove.addActionListener(this::removeCategory);
                                add(remove);
                            }
                        }

                        private void createCategory(ActionEvent actionEvent) {
                            String input = null;
                            do {
                                input = (String) JOptionPane.showInputDialog(
                                        parent,
                                        getString("dialog_resources.insert_category_name"),
                                        getString("dialog_resources.create_category"),
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        null,
                                        input
                                );
                                if (input == null)
                                    return;
                            } while (!DialogUtils.validateInput(parent, input, categoryModel.elements()));
                            addCategory(input);
                            categoryModel.addElement(input);
                            categoryList.setSelectedValue(input, true);
                            onChanged.run();
                        }

                        private void changeCategory(ActionEvent actionEvent) {
                            var category = categoryList.getSelectedValue();
                            var option = JOptionPane.showConfirmDialog(parent,
                                    getString("view.dialog_resources.image.change_category.text", category),
                                    getString("view.dialog_resources.image.change_category.title"),
                                    JOptionPane.YES_NO_OPTION);
                            if(option != JOptionPane.YES_OPTION)
                                return;

                            String input = category;
                            do {
                                input = (String) JOptionPane.showInputDialog(
                                        parent,
                                        getString("view.dialog_resources.image.change_category.text"),
                                        getString("view.dialog_resources.image.change_category.title"),
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        null,
                                        input
                                );
                                if (input == null || input.equals(category))
                                    return;
                            } while (!DialogUtils.validateInput(parent, input, categoryModel.elements()));

                            renameCategory(category, input);

                            var index = categoryList.getSelectedIndex();
                            categoryModel.remove(index);
                            categoryModel.add(index, input);
                            categoryList.setSelectedIndex(index);
                            onChanged.run();
                        }

                        private void removeCategory(ActionEvent actionEvent) {
                            var category = categoryList.getSelectedValue();
                            var option = JOptionPane.showConfirmDialog(parent,
                                    getString("view.dialog_resources.image.delete_category.text", category),
                                    getString("view.dialog_resources.image.delete_category.title"),
                                    JOptionPane.YES_NO_OPTION);
                            if(option != JOptionPane.YES_OPTION)
                                return;
                            var index = categoryList.getSelectedIndex();
                            ResourceList.this.removeCategory(category);
                            categoryModel.remove(index);
                            if(categoryModel.isEmpty())
                                return;
                            if(categoryModel.getSize() <= index)
                                categoryList.setSelectedIndex(index - 1);
                            else
                                categoryList.setSelectedIndex(index);
                            onChanged.run();
                        }

                    }.show(categoryList, e.getX(), e.getY());
                }
            });

            dataList.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() != MouseEvent.BUTTON3 || categoryList.getSelectedIndex() == -1)
                        return;
                    new JPopupMenu(){
                        {
                            var create = new JMenuItem(getString("name.create"));
                            create.addActionListener(this::createData);
                            add(create);
                            if(dataList.getSelectedIndex() != -1){
                                var change = new JMenuItem(getString("name.change"));
                                change.addActionListener(this::changeData);
                                add(change);
                                var move = new JMenuItem(getString("name.move"));
                                move.addActionListener(this::moveData);
                                add(move);
                                var remove = new JMenuItem(getString("name.remove"));
                                remove.addActionListener(this::removeData);
                                add(remove);
                            }
                        }

                        private void createData(ActionEvent actionEvent) {
                            String input = null;
                            do {
                                input = (String) JOptionPane.showInputDialog(
                                        parent,
                                        getString("view.dialog_resources.image.insert_category_name"),
                                        getString("view.dialog_resources.image.create_category"),
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        null,
                                        input
                                );
                                if (input == null)
                                    return;
                            } while (!DialogUtils.validateInput(parent, input, dataModel.elements()));
                            addData(categoryList.getSelectedValue(), input, dataGenerator.get());
                            dataModel.addElement(input);
                            dataList.setSelectedValue(input, true);
                            onChanged.run();
                        }

                        private void changeData(ActionEvent actionEvent) {
                            var data = dataList.getSelectedValue();
                            var option = JOptionPane.showConfirmDialog(parent,
                                    getString("view.dialog_resources.image.change_category.text", data),
                                    getString("view.dialog_resources.image.change_category.title"),
                                    JOptionPane.YES_NO_OPTION);
                            if(option != JOptionPane.YES_OPTION)
                                return;

                            String input = data;
                            do {
                                input = (String) JOptionPane.showInputDialog(
                                        parent,
                                        getString("view.dialog_resources.image.insert_category_name"),
                                        getString("view.dialog_resources.image.change_category.title"),
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        null,
                                        input
                                );
                                if (input == null || input.equals(data))
                                    return;
                            } while (!DialogUtils.validateInput(parent, input, dataModel.elements()));

                            renameData(categoryList.getSelectedValue(), data, input);

                            var index = dataList.getSelectedIndex();
                            dataModel.remove(index);
                            dataModel.add(index, input);
                            dataList.setSelectedIndex(index);
                            onChanged.run();
                        }

                        private void moveData(ActionEvent actionEvent) {
                            var preCategory = categoryList.getSelectedValue();
                            var categories = getSelectionBoxes(false)[0];
                            categories.setSelectedItem(preCategory);
                            if(!DialogUtils.showMultiInputDialog(
                                    parent,
                                    getString("dialog_resources.move_data.title", dataName),
                                    getString("dialog_resources.move_data.text", dataName),
                                    new String[]{ getString("name.category") },
                                    new JComponent[] { categories }
                            ))
                                return;
                            var newCategory = (String) categories.getSelectedItem();
                            var dataName = dataList.getSelectedValue();
                            if(ResourceList.this.containsDataName(newCategory, dataName)){
                                JOptionPane.showMessageDialog(
                                        parent,
                                        getString("dialog_resources.move_data.data_name_exists_already", dataName),
                                        getString("name.error"),
                                        JOptionPane.ERROR_MESSAGE
                                );
                                return;
                            }
                            if(newCategory == null || newCategory.equals(preCategory))
                                return;
                            ResourceList.this.moveData(preCategory, newCategory, dataName);
                            categoryList.setSelectedValue(newCategory, true);
                            dataList.setSelectedValue(dataName, true);
                        }

                        private void removeData(ActionEvent actionEvent) {
                            var data = dataList.getSelectedValue();
                            var option = JOptionPane.showConfirmDialog(parent,
                                    getString("view.dialog_resources.image.delete_category.text", data),
                                    getString("view.dialog_resources.image.delete_category.title"),
                                    JOptionPane.YES_NO_OPTION);
                            if(option != JOptionPane.YES_OPTION)
                                return;
                            var index = dataList.getSelectedIndex();
                            ResourceList.this.removeCategory(data);
                            dataModel.remove(index);
                            if(dataModel.isEmpty())
                                return;
                            if(dataModel.getSize() <= index)
                                dataList.setSelectedIndex(index - 1);
                            else
                                dataList.setSelectedIndex(index);
                            onChanged.run();
                        }

                    }.show(dataList, e.getX(), e.getY());
                }
            });

            return new JList[] { categoryList, dataList };
        }

    }

}
