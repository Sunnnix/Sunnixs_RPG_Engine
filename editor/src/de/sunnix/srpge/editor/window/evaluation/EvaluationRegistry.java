package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.util.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EvaluationRegistry {

    private static final NULLCondition NULL_CONDITION = new NULLCondition();

    private static final Map<String, Supplier<ICondition>> conditions = new HashMap<>();
    private static final Map<String, Supplier<IValueProvider>> providers = new HashMap<>();

    public static void registerCondition(String id, Supplier<ICondition> conditionBuilder){
        conditions.put(id, conditionBuilder);
    }

    public static void registerProvider(String id, Supplier<IValueProvider> providerBuilder){
        providers.put(id, providerBuilder);
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
