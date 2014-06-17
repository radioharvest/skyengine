package aq.oceanbase.skyscroll.loaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import aq.oceanbase.skyscroll.R;

import java.io.*;
import java.util.ArrayList;

public class ShaderLoader {

    private static class Shader {
        private String source;
        private String[] attributes;

        public Shader(String source, String[] attributes) {
            this.source = source;
            this.attributes = attributes;
        }

        public String getSource() {
            return this.source;
        }

        public String[] getAttributes() {
            return this.attributes;
        }


    }

    public ShaderLoader() {

    }

    private static Shader parseSource(String path) throws RuntimeException {
        StringBuilder source = new StringBuilder();
        String currentLine;
        ArrayList<String> attribs = new ArrayList<String>();

        InputStream stream = ShaderLoader.class.getResourceAsStream(path);
        if (stream == null) throw new RuntimeException("Error reading shader");

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        try {
            while ( (currentLine = reader.readLine()) != null) {
                source.append(currentLine).append("\n");
                if (currentLine.contains("attribute")) {
                    String[] tokens = currentLine.split(" ");
                    attribs.add(tokens[tokens.length - 1]);
                }
            }
        } catch (IOException e) {
            Log.e("Shader", "Error reading shader");
        }

        return new Shader(source.toString(), attribs.toArray(new String[0]));
    }


    public static String getShaderSource(String path) {
        StringBuilder shader = new StringBuilder();
        String thisLine;

        InputStream stream = ShaderLoader.class.getResourceAsStream(path);
        if (stream == null) return "NO FILE";

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //if (reader == null) return "nothing bitchslap";

        try {
            while ((thisLine = reader.readLine()) != null) {
                shader.append(thisLine).append("\n");
            }
        } catch (IOException e) {
            Log.e("Shader", "Error reading shader");
        }

        return shader.toString();
    }

    public static int compileShader (final int shaderType, final String shaderSource) {
        int shaderHandler = GLES20.glCreateShader(shaderType);

        if (shaderHandler != 0) {
            GLES20.glShaderSource(shaderHandler, shaderSource);
            GLES20.glCompileShader(shaderHandler);

            final int[] compilationStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandler, GLES20.GL_COMPILE_STATUS, compilationStatus, 0);

            if (compilationStatus[0] == 0) {
                Log.e("Shader", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandler));
                GLES20.glDeleteShader(shaderHandler);
                shaderHandler = 0;
            }
        }

        if (shaderHandler == 0) {
            throw new RuntimeException("Error creating shader");
        }

        return shaderHandler;
    }

    public static int createAndLinkProgram(final int vertexShader, final int fragmentShader, final String[] attributes) {
        int programHandler = GLES20.glCreateProgram();

        if (programHandler != 0) {
            GLES20.glAttachShader(programHandler, vertexShader);
            GLES20.glAttachShader(programHandler, fragmentShader);

            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandler, i, attributes[i]);
                }
            }

            GLES20.glLinkProgram(programHandler);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandler, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                Log.e("Shader", "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandler));
                GLES20.glDeleteProgram(programHandler);
                programHandler = 0;
            }
        }

        if (programHandler == 0) {
            throw new RuntimeException("Error creating program");
        }

        return programHandler;
    }

    public static int getShaderProgram(String pathVertex, String pathFragment) {
        Shader vertex;
        Shader fragment;
        String[] attributes;
        int programHandler;

        try {
            vertex = parseSource(pathVertex);
            fragment = parseSource(pathFragment);
        } catch (RuntimeException e) {
            Log.e("Shader", e.getMessage());
            throw new RuntimeException(e);
        }

        attributes = vertex.getAttributes();

        final int compiledVertex = compileShader(GLES20.GL_VERTEX_SHADER, vertex.getSource());
        final int compiledFragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragment.getSource());

        return createAndLinkProgram(compiledVertex, compiledFragment, attributes);
    }
}
