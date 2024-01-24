package de.sunnix.engine.graphics;

import de.sunnix.engine.debug.GameLogger;
import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;

import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public class Shader extends MemoryHolder {
    private int id;

    @Getter
    private int UNIFORM_PROJECTION;

    public Shader(String shaderPath) {
        loadShaders(shaderPath);
    }

    private void loadShaders(String shaderPath) {
        try {
            var vertSrc = getShaderSrc(shaderPath + ".vert");
            var fragSrc = getShaderSrc(shaderPath + ".frag");

            ContextQueue.addQueue(() -> {
                int vert = 0;
                int frag = 0;
                int program = 0;
                try {
                    vert = compileShader(vertSrc, GL_VERTEX_SHADER);
                    frag = compileShader(fragSrc, GL_FRAGMENT_SHADER);
                    program = glCreateProgram();
                    glAttachShader(program, vert);
                    glAttachShader(program, frag);
                    glLinkProgram(program);

                    if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
                        throw new Exception("Shader Program Linking Failed: " + glGetProgramInfoLog(id));

                    id = program;

                    UNIFORM_PROJECTION = getUniformLocation(this, "projection");
                } catch (Exception e) {
                    GameLogger.logException("Shader", new RuntimeException("Problem creating shader", e));
                    if (program != 0)
                        glDeleteProgram(program);
                } finally {
                    if (vert != 0) {
                        if (program != 0)
                            glDetachShader(program, vert);
                        glDeleteShader(vert);
                    }
                    if (frag != 0) {
                        if (program != 0)
                            glDetachShader(program, frag);
                        glDeleteShader(frag);
                    }
                }
            });
        } catch (Exception e){
            GameLogger.logException("Shader", new RuntimeException("Problem creating shader", e));
        }
    }

    private static String getShaderSrc(String shaderPath) throws Exception{
        try(var stream = Objects.requireNonNull(Shader.class.getResourceAsStream(shaderPath), String.format("Shader file not found (%s)!", shaderPath))){
            return new String(stream.readAllBytes());
        }
    }

    private static int compileShader(String src, int shaderType) throws Exception {
        int shaderID = glCreateShader(shaderType);
        glShaderSource(shaderID, src);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE)
            throw new Exception("Shader Compilation Failed: " + glGetShaderInfoLog(shaderID));

        return shaderID;
    }

    public void bind(){
        glUseProgram(id);
    }

    public void unbind(){
        glUseProgram(0);
    }

    public static int getUniformLocation(Shader shader, String name){
        return glGetUniformLocation(shader.id, name);
    }

    public void uniformMat4(int location, float[] mat){
        glUniformMatrix4fv(location, false, mat);
    }

    @Override
    public boolean isValid() {
        return id != 0;
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.SHADER;
    }

    @Override
    protected String getMemoryInfo() {
        return "Shader";
    }

    @Override
    protected void free() {
        glDeleteProgram(id);
    }

}
