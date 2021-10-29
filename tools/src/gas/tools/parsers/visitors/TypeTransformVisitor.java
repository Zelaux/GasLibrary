package gas.tools.parsers.visitors;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.*;
import gas.tools.gasBlockConverter.*;

public class TypeTransformVisitor<A> extends ModifierVisitor<A>{
    public NameTransform transform;

    public TypeTransformVisitor(NameTransform transform){
        this.transform = transform;
    }

    protected boolean applyTransform(String name, String into){
        return true;
    }

    @Override
    public Visitable visit(ThisExpr n, A arg){
        n.getTypeName().ifPresent(name -> {
            String into = transform.type(name.getIdentifier());
            if(applyTransform(name.getIdentifier(), into)){
                n.setTypeName(new Name(into));
            }
        });
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(SimpleName simpleName, A arg){
        String type = transform.type(simpleName.getIdentifier());
        if(applyTransform(simpleName.getIdentifier(), type)){
            simpleName.setIdentifier(type);
        }
        return super.visit(simpleName, arg);
    }
}
