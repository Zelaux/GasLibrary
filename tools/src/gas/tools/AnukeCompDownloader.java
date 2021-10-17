package gas.tools;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import gas.tools.parsers.*;
import org.apache.commons.io.FileUtils;

import java.net.URL;

public class AnukeCompDownloader {
    private static final String packageName = "gas", annotationsClassName = "GasAnnotations";
    private static final JavaCodeConverter codeConverter = new JavaCodeConverter(false);
    private static String selectedClassName = "";

    public static void main(String[] args) {
        String mindustryVersion = Seq.with(args).find(s -> s.startsWith("v"));
        if (mindustryVersion==null){
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }
        Log.info("Checking Anuke's comps for "+mindustryVersion);
        Fi folder = new Fi("debug");
        try {
            long nanos = System.nanoTime();
            Fi dir = new Fi("core/src/" + packageName + "/entities/compByAnuke");

            if (dir.exists()) dir.delete();
            folder.mkdirs();
            Fi compJava = folder.child("compJava");
            Fi finalComp = folder.child("finalComp");

            LibrariesDownloader.download(mindustryVersion);

            ZipFi sourceZip = LibrariesDownloader.coreZip();

            Fi child = sourceZip.list()[0].child("core").child("src").child("mindustry").child("entities").child("comp");
            for (Fi fi : child.list()) {
                fi.copyTo(compJava.child(fi.name()));
            }
            for (Fi fi : compJava.list()) {
                if (fi.isDirectory()) continue;
                String className = fi.nameWithoutExtension();
                selectedClassName = className;
                if (Seq.with("BuildingComp", "BulletComp", "DecalComp", "EffectStateComp", "FireComp", "LaunchCoreComp", "PlayerComp", "PuddleComp").contains(className)) {
                    compJava.child(fi.name()).delete();
                    finalComp.child(fi.name()).delete();
                    continue;
                }
                if (className.equals("BuildingComp") ||
                    className.equals("BulletComp") ||
                    className.equals("DecalComp") ||
                    className.equals("EffectStateComp") ||
                    className.equals("FireComp") ||
                    className.equals("LaunchCoreComp") ||
                    className.equals("PlayerComp") ||
                    className.equals("PuddleComp") ||
                    className.equals("PosTeamDef")
                ) {
                    Log.info("@ skipped", className);
                    continue;
                }
                String file = fi.readString();
                String convert = codeConverter.convert(file, className);
                String string = convert
                        .replace("var core = team.core();", "mindustry.world.blocks.storage.CoreBlock.CoreBuild core = team.core();")
                        .replace("var core = core();", "mindustry.world.blocks.storage.CoreBlock.CoreBuild core = core();")
                        .replace("var entry = statuses.find(e -> e.effect == effect);", "StatusEntry entry = statuses.find(e -> e.effect == effect);")
                        .replace("package mindustry.entities.comp;", "package " + packageName + ".entities.compByAnuke;")
                        .replace("import static mindustry.logic.GlobalConstants.*;",
                                "import static mindustry.logic.GlobalConstants.*;\n" + "import static mindustry.logic.LAccess.*;")
                        .replace("@Override", "__OVERRIDE__")
                        .replace("@Nullable", "__NULLABLE__")
                        .replace("@", "@" + packageName + ".annotations." + annotationsClassName + ".")
                        .replace("__OVERRIDE__", "@Override")
                        .replace("__NULLABLE__", "@Nullable");
                string = string.replace("};\n" +
                                        "    }", "}\n" +
                                                 "    }");
                finalComp.child(fi.name()).writeString(string);
            }
            Seq<String> names = new Seq<>();
            for (Fi fi : finalComp.list()) {
                fi.copyTo(dir.child(fi.name()));
                names.add(fi.nameWithoutExtension());
            }
            StringBuilder file = new StringBuilder();
            file.append("package " + packageName + ".entities.compByAnuke;\n\n" +
                        "import " + packageName + ".annotations." + annotationsClassName + ";\n" +
                        "import mindustry.gen.Unitc;\n" +
                        "public class AnnotationConfigComponents {");
            for (String name : names) {
                if (!name.endsWith("Comp")) continue;
                String interfaceName = interfaceName(name);
                file.append(Strings.format("@" + annotationsClassName + ".EntitySuperClass\n" +
                                           "    public static interface @ extends mindustry.gen.@{\n" +
                                           "    }", "@", interfaceName, interfaceName)).append("\n");
            }
            file.append("\n}");
            dir.child("AnnotationConfigComponents.java").writeString(file.toString());
            System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        folder.deleteDirectory();
        folder.walk(f -> f.delete());
        folder.delete();
        System.exit(0);
    }

    static String interfaceName(String comp) {
        String suffix = "Comp";

        //example: BlockComp -> IBlock
        return comp.substring(0, comp.length() - suffix.length()) + "c";
    }

    private static Seq<String> transform(String line, Seq<String> out) {
        out = new Seq<>();
        boolean debug = selectedClassName.equals("BuilderComp") && false;
        if (line.contains("return switch")) {
            String[] split = line.split("\n");
            StringBuilder newLine = new StringBuilder();
            boolean lineReturn = false;
            int opened = 0;
            for (String s : split) {
                String l = s
                        .replace("return switch", "switch")
                        .replace(" -> ", ":\nreturn ");
                if (l.contains(":\nreturn ")) {
                    lineReturn = true;
//                    newLine.append(l, 0, l.indexOf("\nreturn")).append("\nreturn");
                }
                if (l.contains(";")) lineReturn = false;
                newLine.append(l);
                if (!lineReturn) newLine.append("\n");
            }
            String s1 = newLine.toString();
            StringBuilder g = new StringBuilder(s1.split("\n", 2)[0]);
            for (String s2 : s1.split("\n", 2)[1].split("\n")) {
                String tran = transform(s2);
//                Log.info("[@]->[@]", s2, tran);
                g.append(tran).append("\n");
            }
            out.add(g.toString());
            return out;
        }
        try {
            if (line.contains("instanceof ")) {
                line = line.replace("\n\n", "\n");
                if (line.contains("->")) {
                    int brackets = 0;
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == '(') brackets++;
                        if (c == ')') brackets--;
                    }
                    boolean monoLine = brackets == 0;
                    String preCenter = line.substring(0, line.indexOf('('));
                    String center = line.substring(line.indexOf('('), line.lastIndexOf(')') + 1);
                    String postCenter = line.substring(line.lastIndexOf(')'));
                    if (monoLine) {
                        boolean curlyBraces = center.substring(center.indexOf("->")).replace(" ", "").startsWith("{");
                        if (curlyBraces) {
                            String part1 = center.substring(0, center.indexOf("{") + 1);
                            String partCode = center.substring(center.indexOf("{") + 1, center.lastIndexOf("}"));
                            String part2 = center.substring(center.lastIndexOf("}"));
                            line = preCenter + part1 + transform(partCode) + part2 + postCenter;
                        } else {
                            String part1 = center.substring(0, center.indexOf("->") + 2) + "{";
                            String partCode = "return " + center.substring(center.indexOf("->") + 2, center.lastIndexOf(")")) + ";";
                            String part2 = "}";
                            line = preCenter + part1 + transform(partCode) + part2 + postCenter;
                        }
                    } else {
                        //            controlling.removeAll(u -> u.dead || !(u.controller() instanceof FormationAI ai && ai.leader == self()));
                        throw new RuntimeException("It's not monoline, I don't know what to do!!!");
                    }
                } else {
                    String deb = line.split(" instanceof", 2)[0];
                    if (debug) {
                        Log.info("line: @", line);
                    }
                    int opened = 0;
                    String instanceName = "";
                    String[] g17 = deb.split("");
                    if (deb.endsWith(" self()")) instanceName = "self()";
                    String returnCheck = deb.contains("return") ? deb.substring(deb.indexOf("return ")) : "";
                    if (returnCheck.split(" ").length == 2 && returnCheck.startsWith("return "))
                        instanceName = returnCheck.substring(returnCheck.indexOf(" ") + 1);
                    if (deb.substring(0, deb.lastIndexOf(" ")).replace(" ", "").equals("")) instanceName = deb;
                    for (int i = g17.length - 1; i >= 0; i--) {
                        String symbol = g17[i];
                        if (symbol.equals(")")) {
                            opened++;
                        }
                        if (opened > 0) {
                            if (symbol.equals("(")) opened--;
                        } else if (symbol.equals("(") || symbol.equals("&") || symbol.equals("|") || symbol.equals("?") || symbol.equals(":")) {
                            instanceName = deb.substring(i + 1);
                            break;
                        }

                    }

                    if (debug) {
                        Log.info("instanceName: @", instanceName);
                    }
                    if (instanceName.equals("")) {
                        out.clear();
                        out.add(line);
                        return out;
                    }
//                Log.info(instanceName);
                    String[] split = line.split("instanceof ", 2)[1].split(" ", 2);
                    String[] strings = split[1].split("");
                    StringBuilder variableBuild = new StringBuilder();
                    Seq<String> with = Seq.with(")", "&", "|", "?");
                    for (int i = 0; i < strings.length; i++) {
                        String string = strings[i];
                        if (with.contains(string)) break;
                        variableBuild.append(string);
                    }
                    String variableName = variableBuild.toString();
                    if (debug) {
                        Log.info("variableName: @", variableName);
                    }
                    if (variableName.replace(" ", "").equals("")) {
                        out.clear();
                        out.add(line);
                        return out;
                    }
                    int index = split[1].indexOf(variableName);
                    String part1 = line.substring(0, line.indexOf("instanceof ") + "instanceof ".length()) + split[0] + split[1].substring(0, index);
                    String part2 = split[1].substring(index + variableName.length());
                    String l = " && (" + variableName + " = (" + split[0] + ")" + instanceName + ")==" + instanceName + "\n", nl = split[0] + " " + variableName.replace(" ", "") + ";";
//            Log.info("===@",part1);
                    out.add(nl);
                    line =/* nl + "\n" +*/ part1 + l + part2;
//            Log.info("==@",Seq.with(split).toString(","));
                    if (split.length > 3 && !split[2].equals("&&") && !split[2].equals("||") && !split[2].equals(")") && !split[2].equals("}")) {
                    }
                }
            }
        } catch (Exception e) {
            ;
            if (debug) {
                Log.err("cannot transform line: [" + line + "] reason: @", e);
            } else {
                Log.err("cannot transform line: [@]", line);
            }
        }
//       line= line.replace(" : "," \n: ");
        line = line.replace("};\n" +
                            "    }", "}\n}");
        if (line.contains("\n")) {
            StringBuilder nl = new StringBuilder(), prel = new StringBuilder();
            for (String s : line.split("\n")) {
                Seq<String> transform = transform(s, null);
                if (transform.size > 1) {
                    out.set(0, out.get(0) + "\n" + transform.get(0));
                    if (out.size == 2) {
                        out.set(1, out.get(1) + "\n" + transform.get(1));
                    } else {
                        out.add(transform.get(1));
                    }
                } else {
                    if (out.size == 2) {
                        out.set(1, out.get(1) + "\n" + transform.get(0));
                    } else {
                        out.add(transform.get(0));
                    }

                }
            }
            line = prel.toString() + nl.toString();
        }


        out.add(line);
//        return line;
        return out;
    }

    private static String transform(String line) {
        Seq<String> transform = transform(line, null);
        return transform.toString("\n");
    }

    private static String strip(String str) {
        while (str.startsWith(" ")) {
            str = str.substring(1);
        }
        while (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private static boolean isMethod(String mline) {
        return mline.contains("(") && mline.contains(")") && mline.contains("{") && mline.startsWith("    ") && !mline.substring(4).startsWith("    ");
    }
}