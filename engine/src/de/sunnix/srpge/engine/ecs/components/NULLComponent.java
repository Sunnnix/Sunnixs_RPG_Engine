package de.sunnix.srpge.engine.ecs.components;

public final class NULLComponent extends Component{

    private static NULLComponent INSTANCE;

    private NULLComponent(){};

    public static NULLComponent getNULL(){
        if(INSTANCE == null)
            INSTANCE = new NULLComponent();
        return INSTANCE;
    }

}
