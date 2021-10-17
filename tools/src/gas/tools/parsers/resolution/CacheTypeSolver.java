package gas.tools.parsers.resolution;

import arc.struct.*;
import com.github.javaparser.*;
import com.github.javaparser.ParseResult.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.symbolsolver.javaparser.*;
import com.github.javaparser.symbolsolver.javaparsermodel.*;
import com.github.javaparser.symbolsolver.model.resolution.*;

import java.util.*;

public class CacheTypeSolver implements TypeSolver , PostProcessor{

    private TypeSolver parent;
    private final ObjectMap<String, SymbolReference<ResolvedReferenceTypeDeclaration>> cacheMap=new ObjectMap<>();
    private final ObjectMap<String,CompilationUnit> compilationUnits =new ObjectMap<>();
    @Override
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(TypeSolver parent) {
        Objects.requireNonNull(parent);
        if (this.parent != null) {
            throw new IllegalStateException("This TypeSolver already has a parent.");
        }
        if (parent == this) {
            throw new IllegalStateException("The parent of this TypeSolver cannot be itself.");
        }
        this.parent = parent;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name){

        SymbolReference<ResolvedReferenceTypeDeclaration> result=cacheMap.getNull(name);
        if (result==null){
            result=tryToSolveTypeUncached(name);
            if (result.isSolved()){
                cacheMap.put(name,result);
            }
        }
        return result;
    }

    private SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveTypeUncached(String typeName){
        for(CompilationUnit compilationUnit : compilationUnits.values()){
            Optional<com.github.javaparser.ast.body.TypeDeclaration<?>> astTypeDeclaration = Navigator
            .findType(compilationUnit, typeName);
            if (astTypeDeclaration.isPresent()) {
                return SymbolReference
                .solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
            }
        }
        return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
    }

    public void addCompilationUnit(String path,CompilationUnit compilationUnit){
        compilationUnits.put(path,compilationUnit);
        for(String key : cacheMap.keys().toSeq()){
            if (key.startsWith(path))cacheMap.remove(key);
        }
    }
public void addCompilationUnit(CompilationUnit compilationUnit){
    if(compilationUnit.getPackageDeclaration().isPresent()){
        compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
        .flatMap(ClassOrInterfaceDeclaration::getFullyQualifiedName)
        .ifPresent(fullName -> {
            addCompilationUnit(fullName, compilationUnit);
        });
//                    addCompilationUnit(first);
    }
}
    @Override
    public void process(ParseResult<? extends Node> result, ParserConfiguration configuration){
        result.ifSuccessful(node -> {
            if (node instanceof CompilationUnit){
                addCompilationUnit((CompilationUnit)node);
            }
        });
    }
}
