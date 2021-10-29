package gas.tools.gasBlockConverter;

public record ClassInfo(String packageName,String className){
    public String fullName(){
        return packageName+"."+className;
    }
}
