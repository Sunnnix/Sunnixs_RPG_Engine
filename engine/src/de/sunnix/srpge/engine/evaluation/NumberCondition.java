package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;

import java.util.Optional;

import static de.sunnix.srpge.engine.util.FunctionUtils.EPSILON;

public class NumberCondition extends Condition<Number> {

    protected enum NumEvalType {
        EQUAL("="), GREATER(">"), SMALLER("<");

        public final String text;

        NumEvalType(String text){
            this.text = text;
        }
    }

    protected NumEvalType type = NumEvalType.EQUAL;
    protected Number number = 0;

    public NumberCondition(){
        super("number");
    }

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        type = NumEvalType.values()[dso.getByte("type", (byte) NumEvalType.EQUAL.ordinal())];
        number = dso.getDouble("number", 0);
    }

    @Override
    public boolean evaluate(World world) {
        if(provider == null)
            return false;
        var value = provider.getValue(world);
        return switch (type) {
            case EQUAL -> Math.abs(value.doubleValue() - number.doubleValue()) < EPSILON;
            case GREATER -> value.doubleValue() > number.doubleValue() + EPSILON;
            case SMALLER -> value.doubleValue() < number.doubleValue() - EPSILON;
        };
    }
}
