package table.treetable.fitlered;

import java.util.function.Predicate;

import javafx.scene.control.TreeItem;


@FunctionalInterface
public interface TreeItemPredicate<T> {
 
    boolean test(TreeItem<T> parent, T value);
 
    static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
        return (parent, value) -> predicate.test(value);
    }
 
}