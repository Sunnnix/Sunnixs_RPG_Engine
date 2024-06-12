package de.sunnix.srpge.engine.graphics;

import de.sunnix.srpge.engine.memory.ContextQueue;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class TestCubeRenderObject {

    private int vertexArray;

    private boolean inited;

    public TestCubeRenderObject(){
        ContextQueue.addQueue(() -> {
            vertexArray = glGenVertexArrays();
            glBindVertexArray(vertexArray);

            var verticesID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, verticesID);
            glBufferData(GL_ARRAY_BUFFER, new float[] {
                    -.01f, -.01f, 0f,
                    -.01f,  .01f, 0f,
                    .01f,  .01f, 0f,
                    .01f, -.01f, 0f
            }, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            var elementBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[]{
                    0, 1, 3,
                    1, 2, 3
            }, GL_STATIC_DRAW);

            unbind();
            inited = true;
        });
    }

    public void bind(){
        glBindVertexArray(vertexArray);
    }

    public void unbind(){
        glBindVertexArray(0);
    }

    public void render(){
        if(!inited)
            return;
        Shader.TEST_CUBE_SHADER.bind();
        bind();
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

}
