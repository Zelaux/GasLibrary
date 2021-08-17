package gas.tools.gasBlockConverter.visitors;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.visitor.*;

import java.util.*;

public abstract class CustomModifierVisitor extends ModifierVisitor<Void>{

    protected  <N extends Node> NodeList<N> modifyList(NodeList<N> list, Void arg) {
        return (NodeList<N>) list.accept(this, arg);
    }

    protected  <N extends Node> NodeList<N> modifyList(Optional<NodeList<N>> list, Void arg) {
        return list.map(ns -> modifyList(ns, arg)).orElse(null);
    }
}
