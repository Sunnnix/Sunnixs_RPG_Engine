package de.sunnix.srpge.editor.window.customswing;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.events.ObjectValue;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

import static de.sunnix.srpge.editor.lang.Language.getString;

/**
 * A panel component for selecting game objects using a spinner and a button
 * to open a selection dialog. The user can select an object by its ID or a
 * variable (local or global) and displays the object's name in a non-editable
 * text field. The panel supports various selection modes, including player
 * selection and variable-based selection.
 */
public class ObjectPicker extends JPanel {

    private final Window window;
    private final MapData map;
    private final GameObject currentObject;
    private final ObjectValue ov;
    private final JSpinner spinner;
    private final JTextField objectName;
    private final JButton selectObjectBtn;

    /**
     * Creates an {@link ObjectPicker} panel.
     *
     * @param window       the main application window
     * @param map          the current game map containing game objects (can be null)
     * @param allowPlayer  whether the player object should be included in the selection
     * @param currentObject the currently selected game object, or {@code null} if none
     * @param ov            the ObjectValue that holds the object receiver data
     * @param allowVariables whether variables can be selected as object references
     */
    public ObjectPicker(Window window, MapData map, boolean allowPlayer, GameObject currentObject, ObjectValue ov, boolean allowVariables) {
        this.window = window;
        this.map = map;
        this.currentObject = currentObject;
        this.ov = ov.clone();
        setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();
        gbc.fill = GridBagConstraints.BOTH;

        // Object name field
        objectName = createObjectNameField();
        add(objectName, gbc);

        // Spinner configuration
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;

        spinner = createObjectSpinner(allowPlayer);
        add(spinner, gbc);

        // Object selection button
        gbc.gridx++;
        selectObjectBtn = createSelectObjectButton(window, map, allowPlayer, currentObject, allowVariables);
        add(selectObjectBtn, gbc);;

        // Listeners
        spinner.addChangeListener(e -> {
            var value = ((Number) spinner.getValue()).intValue();
            switch (this.ov.getType()){
                case ID -> this.ov.setObject(value);
                case GLOBAL_VAR -> this.ov.setGlobalVar(value);
                case LOCAL_VAR -> this.ov.setLocalVar(ov.getObject(), value);
            }
            updateObjectName(window, map);
        });

        // Initialize values
        updateObjectName(window, map);
    }

    /**
     * Creates an {@link ObjectPicker} panel with default variable selection allowed.
     *
     * @param window       the main application window
     * @param map          the current game map containing game objects (can be null)
     * @param allowPlayer  whether the player object should be included in the selection
     * @param currentObject the currently selected game object, or {@code null} if none
     * @param ov            the ObjectValue that holds the object receiver data
     */
    public ObjectPicker(Window window, MapData map, boolean allowPlayer, GameObject currentObject, ObjectValue ov) {
        this(window, map, allowPlayer, currentObject, ov, true);
    }

    /**
     * Creates the non-editable text field that displays the object's name.
     *
     * @return the text field to display the object name
     */
    private JTextField createObjectNameField() {
        var objectNameField = new JTextField(16);
        objectNameField.setEditable(false);
        return objectNameField;
    }

    /**
     * Creates the spinner used to select object IDs.
     *
     * @param allowPlayer whether to include player object (ID 999) in the selection
     * @return a configured {@link JSpinner} for selecting object IDs
     */
    private JSpinner createObjectSpinner(boolean allowPlayer) {
        var model = genSpinnerModel(allowPlayer);
        var spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        spinner.setPreferredSize(new Dimension(55, 25));
        return spinner;
    }

    /**
     * Creates the button to open the object selection dialog.
     *
     * @param window        the main application window
     * @param map           the current game map containing game objects
     * @param allowPlayer   whether to include the player object in the selection
     * @param currentObject the currently selected game object
     * @param allowVariables whether to allow selecting variables
     * @return a button that opens the object selection dialog
     */
    private JButton createSelectObjectButton(Window window, MapData map, boolean allowPlayer, GameObject currentObject, boolean allowVariables) {
        var selectObjectBtn = new JButton("...");
        selectObjectBtn.addActionListener(l -> {

            new JDialog(DialogUtils.getWindowForComponent(this), "Select object", Dialog.ModalityType.APPLICATION_MODAL){
                {
                    var panel = new JPanel(new BorderLayout(5, 5));
                    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    setContentPane(panel);

                    var typeSelect = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                    typeSelect.setBorder(BorderFactory.createTitledBorder("Provider type"));
                    var group = new ButtonGroup();
                    var type_object = new JRadioButton("Object");
                    group.add(type_object);
                    type_object.setEnabled(map != null);
                    typeSelect.add(type_object);
                    var type_glob_var = new JRadioButton("Global variable");
                    type_glob_var.setEnabled(allowVariables);
                    group.add(type_glob_var);
                    typeSelect.add(type_glob_var);
                    var type_loc_var = new JRadioButton("Local variable");
                    type_loc_var.setEnabled(map != null && allowVariables);
                    group.add(type_loc_var);
                    typeSelect.add(type_loc_var);

                    add(typeSelect, BorderLayout.NORTH);

                    final var content = new JPanel(new BorderLayout());
                    add(content, BorderLayout.CENTER);

                    var list = new ArrayList<>(map.getObjects());
                    if (allowPlayer) list.add(0, window.getPlayer());
                    var objectCombo = new JComboBox<>(list.toArray(GameObject[]::new));
                    objectCombo.setPreferredSize(new Dimension(0, 25));


                    var index = ov.getObject();
                    var selectedObject = (index == -1) ? currentObject : map.getObject(index);
                    objectCombo.setSelectedItem(selectedObject);

                    var indexSpinner = new JSpinner();
                    indexSpinner.setPreferredSize(new Dimension(45, 25));
                    indexSpinner.setEditor(new JSpinner.NumberEditor(indexSpinner, "#"));

                    var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    var apply = new JButton(getString("button.apply"));
                    apply.addActionListener(l -> {
                        if(type_object.isSelected()) {
                            ov.setObject(((GameObject) objectCombo.getSelectedItem()).getID());
                            spinner.setModel(genSpinnerModel(allowPlayer));
                        } else if(type_glob_var.isSelected()) {
                            ov.setGlobalVar(((Number) indexSpinner.getValue()).intValue());
                            spinner.setModel(genSpinnerModel(allowPlayer));
                        } else {
                            ov.setLocalVar(((GameObject) objectCombo.getSelectedItem()).getID(), ((Number) indexSpinner.getValue()).intValue());
                            spinner.setModel(genSpinnerModel(allowPlayer));
                        }
                        // bugfix, that the displayed number is correct
                        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
                        updateObjectName(window, map);
                        spinner.repaint();
                        dispose();
                    });
                    buttons.add(apply);
                    var cancel = new JButton(getString("button.cancel"));
                    cancel.addActionListener(l -> dispose());
                    buttons.add(cancel);

                    add(buttons, BorderLayout.SOUTH);

                    var reset = (Runnable) () -> {
                        content.remove(objectCombo);
                        content.remove(indexSpinner);
                    };

                    // listeners
                    type_object.addActionListener(l -> {
                        reset.run();
                        content.add(objectCombo, BorderLayout.CENTER);
                        repaint();
                        revalidate();
                    });
                    type_glob_var.addActionListener(l -> {
                        reset.run();
                        var max = Variables.getIntsSize() - 1;
                        indexSpinner.setModel(new SpinnerNumberModel(Math.min(max, ov.getIndex()), 0, max, 1));
                        indexSpinner.setEditor(new JSpinner.NumberEditor(indexSpinner, "#"));
                        content.add(indexSpinner, BorderLayout.CENTER);
                        repaint();
                        revalidate();
                    });
                    type_loc_var.addActionListener(l -> {
                        reset.run();
                        content.add(objectCombo, BorderLayout.CENTER);
                        var max = de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1;
                        indexSpinner.setModel(new SpinnerNumberModel(Math.min(max, ov.getIndex()), 0, max, 1));
                        indexSpinner.setEditor(new JSpinner.NumberEditor(indexSpinner, "#"));
                        content.add(indexSpinner, BorderLayout.EAST);
                        repaint();
                        revalidate();
                    });

                    // variables
                    switch (ov.getType()){
                        case ID -> type_object.doClick();
                        case GLOBAL_VAR -> type_glob_var.doClick();
                        case LOCAL_VAR -> type_loc_var.doClick();
                    }

                    setResizable(false);
                    pack();
                    setLocationRelativeTo(ObjectPicker.this);
                    setVisible(true);
                }
            };
        });
        selectObjectBtn.setEnabled(map != null);
        return selectObjectBtn;
    }

    /**
     * Updates the object name displayed in the text field based on the selected
     * object ID, global variable, or local variable.
     *
     * @param window the main application window
     * @param map    the current game map containing game objects
     */
    private void updateObjectName(Window window, MapData map) {
        switch (ov.getType()){
            case ID -> {
                int id = ov.getObject();
                if (id == -1) {
                    objectName.setForeground(Color.GREEN);
                    objectName.setText("Self");
                } else if (id == 999) {
                    objectName.setForeground(Color.CYAN);
                    objectName.setText(window.getPlayer().toString());
                } else {
                    var obj = (map == null) ? null : map.getObject(id);
                    objectName.setForeground(UIManager.getColor("TextField.foreground"));
                    objectName.setText(obj == null ? "" : obj.toString());
                }
            }
            case GLOBAL_VAR -> {
                objectName.setForeground(Color.ORANGE);
                objectName.setText("Global: " + Variables.getIntName(ov.getIndex()));
            }
            case LOCAL_VAR -> {
                objectName.setForeground(Color.YELLOW);
                objectName.setText("Local: " + map.getObject(ov.getObject()));
            }
        }
    }

    /**
     * Generates a spinner model for object ID or variable selection.
     *
     * @param allowPlayer whether to allow selecting the player object (ID 999)
     * @return a {@link SpinnerNumberModel} for selecting IDs or variables
     */
    private SpinnerNumberModel genSpinnerModel(boolean allowPlayer){
        return switch (this.ov.getType()){
            case ID -> new SpinnerNumberModel(this.ov.getObject(), -1, allowPlayer ? 999 : 998, 1);
            case GLOBAL_VAR -> new SpinnerNumberModel(Math.min(Variables.getIntsSize() - 1, this.ov.getIndex()), 0, Variables.getIntsSize() - 1, 1);
            case LOCAL_VAR -> new SpinnerNumberModel(Math.min(de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1, this.ov.getIndex()), 0, de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1, 1);
        };
    }

    /**
     * @return the updated {@link ObjectValue}
     */
    public ObjectValue getNewValue(){
        return ov;
    }

    /**
     * Returns the game object based on the current selection.
     *
     * @return the selected {@link  GameObject}, or <b>null</b> if none
     */
    public GameObject getGameObject(){
        return switch (ov.getObject()){
            case -1 -> currentObject;
            case 999 -> window.getPlayer();
            default -> map.getObject(ov.getObject());
        };
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        spinner.setEnabled(enabled);
        if(selectObjectBtn != null)
            selectObjectBtn.setEnabled(enabled);
    }

    /**
     * Adds a {@link ChangeListener} to monitor changes in the spinner's value.
     *
     * @param listener the {@link ChangeListener} to add
     */
    public void addChangeListener(ChangeListener listener){
        spinner.addChangeListener(listener);
    }

}
