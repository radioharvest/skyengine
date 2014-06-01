package aq.oceanbase.skyscroll.render;

import android.opengl.GLES20;
import android.util.Log;

public class GraphicsCommons {

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
}
