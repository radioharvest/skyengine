package aq.oceanbase.skyscroll.loaders;

import android.content.Context;
import android.util.Log;

import java.io.*;

public class ShaderLoader {

    private class Shader {
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



    public static String getShaderSource(String path) {
        StringBuilder shader = new StringBuilder();
        path = "/aq/oceanbase/skyscroll" + path;
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

    public int getShaderProgram(String pathVertex, String pathFragment) {

        return 1;
    }
}
