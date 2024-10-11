package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.engine.util.Tuple;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

public class EvaluationRegistry {

    public enum Type {
        NUMBER, BOOL
    }

    private static final NULLCondition NULL_CONDITION = new NULLCondition();

    private static final Map<String, Supplier<ICondition>> conditions = new HashMap<>();
    private static final Map<String, Supplier<IValueProvider>> providers = new HashMap<>();

    private static final Map<Type, List<Tuple.Tuple2<String, String>>> typedProviders = new HashMap<>();

    public static void registerCondition(String id, Supplier<ICondition> conditionBuilder){
        conditions.put(id, conditionBuilder);
    }

    public static void registerProvider(String id, String name, Type type, Supplier<IValueProvider> providerBuilder){
        providers.put(id, providerBuilder);
        typedProviders.computeIfAbsent(type, k -> new ArrayList<>()).add(new Tuple.Tuple2<>(id, name));
    }

    public static ICondition createCondition(String id){
        var builder = conditions.get(id);
        if(builder == null)
            return NULL_CONDITION;
        return builder.get();
    }

    public static ICondition loadCondition(String id, DataSaveObject dso){
        var condition = createCondition(id);
        condition.load(dso);
        return condition;
    }

    public static IValueProvider createProvider(String id){
        var builder = providers.get(id);
        if(builder == null)
            return null;
        return builder.get();
    }

    public static IValueProvider loadProvider(String id, DataSaveObject dso){
        var provider = createProvider(id);
        if(provider != null)
            provider.load(dso);
        return provider;
    }

    public static ICondition showConditionCreateDialog(JComponent parent){
        var dialog = new ConditionCreateDialog(DialogUtils.getWindowForComponent(parent));
        return dialog.condition;
    }

    public static List<Tuple.Tuple2<String, String>> getTypeProviders(Type type){
        return typedProviders.getOrDefault(type, Collections.emptyList());
    }

    public static JComboBox<Tuple.Tuple2<String, String>> getTypeProvidersCombo(Type type, String defaultID){
        var typedProviders = getTypeProviders(type);
        var model = new DefaultComboBoxModel<Tuple.Tuple2<String, String>>();
        var combo = new JComboBox<>(model);
        model.addAll(typedProviders);
        var renderer = combo.getRenderer();
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            var label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(value.t2());
            return label;
        });
        var index = FunctionUtils.indexOf(typedProviders, t -> Objects.equals(t.t1(), defaultID));
        if(index == -1)
            index = 0;
        combo.setSelectedIndex(index);
        return combo;
    }

    private static class ConditionCreateDialog extends JDialog{

        ICondition condition;

        ConditionCreateDialog(Window parent){
            super(parent, "Create condition", ModalityType.APPLICATION_MODAL);
            ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new GridLayout(0, 1));
            for(var registry: conditions.keySet()) {
                var btn = new JButton(registry);
                btn.addActionListener(l -> {
                    condition = createCondition(registry);
                    dispose();
                });
                add(btn);
            }
            setResizable(false);
            pack();
            setLocationRelativeTo(parent);
            setVisible(true);
        }

    }

}
