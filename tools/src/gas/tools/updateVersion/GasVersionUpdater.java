package gas.tools.updateVersion;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mma.tools.parsers.*;

import java.io.*;
import java.util.zip.*;

public class GasVersionUpdater{
    static String mindustryVersion;
    static Seq<String> argsSeq;

    public static void main(String[] args){
        argsSeq = Seq.with(args);
        mindustryVersion = argsSeq.find(s -> s.startsWith("v"));
        if(mindustryVersion == null){
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }

        LibrariesDownloader.download(mindustryVersion);

        runTask("Starting creating gasBlocks for version " + mindustryVersion, CreatingGasBlocks::run);


    }

    private static void runTask(String name, ThrowCons2<String, String[]> task){
        System.out.println(name);
        long nanos = System.nanoTime();
        try{
            task.get(mindustryVersion, argsSeq.toArray(String.class));
        }catch(Exception e){
            System.out.println("[ERROR]CANNOT RUN TAST " + name);
            e.printStackTrace();
            return;
        }
        System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        System.out.println();

    }

    private static void writeZip(ZipOutputStream stream, Fi fi, String name) throws IOException{
        stream.putNextEntry(new ZipEntry(name));
        stream.write(fi.readBytes());
        stream.closeEntry();
    }

    interface ThrowCons2<T, N>{
        void get(T t, N n) throws Exception;
    }
}
