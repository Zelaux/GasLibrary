GasLirary
========
`GasLirary` is a Mindustry java mod library for making new content type: Gas.
<img width="960" alt="banner2" src="https://user-images.githubusercontent.com/58040045/128306487-250b2dab-ded6-4fa0-a582-d4e99bb67098.png">

### Usage/Examples
###### Gas creating
Gas is created in the same way as Item or Liquid, but in terms of parameters it is very similar to Liquid
YourGasses.java
```java
package example;

import arc.graphics.Color;
import gas.type.Gas;
import mindustry.ctype.ContentList;

public class YourGasses implements ContentList {
    public static Gas oxygen;
    @Override
    public void load() {
        oxygen=new Gas("oxygen"){{
            color = Color.valueOf("bcf9ff");
            flammability = 0.7f;
            explosiveness = 0.9f;
        }};
    }
}
```

###### Adding gas to block consumes
Add this line
```java
consumes.addGas(new ConsumeGas(YourGasses.gas, amount));
``` 
to your block initialization like this:
```java
new GasBlock("your-block"){{
    consumes.addGas(new ConsumeGas(YourGasses.gas, amount));
}};
```

You can see all the classes and static method added by the mod [here](https://github.com/Zelaux/GasLibrary/blob/master/AllClassesAndMethods.md "All classes and method")
If you did not find the class you need, then you can make a Pull request or Issues.

## Mindustry Mod By Zelaux

## Resources
- [Last Releases](https://github.com/Zelaux/GasLirary/releases)

## Authors
- Zelaux(main-programmer)


# Installation Guide
## 1.Via .jar File
* 1.Go to [releases](https://github.com/Zelaux/GasLirary/releases).

* 2.Download the latest .jar file.

* 3.Launch Mindustry.

* 4.Open "Mods".

* 5."Import Mod".

* 6."Impot File"

* 7.Find file with name "GasLirary.jar" and click "load".

* 8.Play!

## 2.Via Mod Browser
* 1.Go to in-game Mod Browser.

* 2.Find "GasLirary" in mod list.

* 3.Download.  

# Build Guide

## PC

* 1.Download intelijIDEA.

* 2.Clone this repository.

* 3.When importing is end, go to Intelij console and type:

Windows      |  MacOSX       | Linux
------------ | ------------- | -------------
gradlew jar  | ./gradlew jar | ./gradlew jar

* 4.When compilation is end, your build will be in "build/libs"
Download
--------

Depend via Maven:
```xml
<dependency>
	    <groupId>com.github.Zelaux.GasLirary</groupId>
	    <artifactId>core</artifactId>
	    <version>v1.4</version>
</dependency>
```
or Gradle:
```groovy
dependencies {
        implementation 'com.github.Zelaux.GasLirary:v1.4'
}
```

And don't forget to add the dependency to your mod.(h)json
```hjson
dependencies: ["gas-library-java"]
```
