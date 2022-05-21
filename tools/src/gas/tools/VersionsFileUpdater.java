package gas.tools;

import arc.files.*;
import arc.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class VersionsFileUpdater{
    public static void main(String[] args)  throws Exception{
        Fi versions = Fi.get("versions");

        Process proc = Runtime.getRuntime().exec("git log --pretty=format:\"%H:%s\" -1");
        proc.waitFor();

//            String result = new String(.readNBytes(Integer.MAX_VALUE));
        String result = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
        if (result.matches("[\"].*[\"]")){
            result=result.substring(1,result.length()-1);
        }
        String gameVersion = Structs.find(args, v -> v.startsWith("v_"));
        if (gameVersion == null) {
            throw new RuntimeException("cannot find gameVersion from " + Arrays.toString(args));
        }
        gameVersion=gameVersion.substring("v_".length());
        String version = result.substring(0, 11);
        Log.info("result(@), version(@)",result, version);
        versions.child(gameVersion + ".txt").writeString(version);
        versions.child("lastVersion.txt").writeString(version);
        try {
            new URL("https://jitpack.io/com/github/Zelaux/GasLibrary/" +version + "/build.log").openStream();
        } catch (ConnectException exception) {
            exception.printStackTrace();
        }
    }
}
