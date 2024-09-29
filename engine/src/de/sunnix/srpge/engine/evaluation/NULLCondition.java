package de.sunnix.srpge.engine.evaluation;

import de.sunnix.srpge.engine.ecs.World;

public class NULLCondition extends Condition<Object> {

    public NULLCondition() {
        super(null);
    }

    @Override
    public boolean evaluate(World world) {
        return false;
    }
}
