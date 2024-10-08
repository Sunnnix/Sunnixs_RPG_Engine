package de.sunnix.srpge.engine.graphics;

import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.memory.ContextQueue;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public class Shader extends MemoryHolder {

    public static final Shader DEFAULT_SHADER = genDefaultShader();
    public static final Shader TEST_CUBE_SHADER = new Shader("/data/shader/test_cube_shader");

    private static int activeShader = 0;

    private int id;
    private Map<String, Integer> uniformLocations = new HashMap<>();

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

                    getUniformLocation("projection");
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

    /**
     * Binds this Shader to the current context
     * @return if the shader has been bound
     */
    public boolean bind(){
        if(activeShader != id) {
            glUseProgram(id);
            activeShader = id;
            return true;
        }
        return false;
    }

    public void unbind(){
        if(activeShader != 0) {
            glUseProgram(0);
            activeShader = 0;
        }
    }

    private static int getUniformLocation(Shader shader, String name){
        return glGetUniformLocation(shader.id, name);
    }

    private int getUniformLocation(String name){
        return uniformLocations.computeIfAbsent(name, k -> getUniformLocation(this, k));
    }

    public void uniformMat4(String name, float[] mat){
        glUniformMatrix4fv(getUniformLocation(name), false, mat);
    }

    public void uniform4f(String name, float v1, float v2, float v3, float v4) {
        glUniform4f(getUniformLocation(name), v1, v2, v3, v4);
    }

    public void uniform4f(String name, Vector4f vec4){
        uniform4f(name, vec4.x, vec4.y, vec4.z, vec4.w);
    }

    public void uniform3f(String name, float v1, float v2, float v3) {
        glUniform3f(getUniformLocation(name), v1, v2, v3);
    }

    public void uniform3f(String name, Vector3f vec3){
        uniform3f(name, vec3.x, vec3.y, vec3.z);
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

    public static Shader genDefaultShader(){
        return new Shader("/data/shader/default_shader");
    }

}
