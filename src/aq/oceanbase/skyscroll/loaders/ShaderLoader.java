package aq.oceanbase.skyscroll.loaders;

import android.util.Log;

import java.io.*;

public class ShaderLoader {

    public ShaderLoader() {

    }

    public static String getShader(String path) {
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
}
