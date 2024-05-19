package de.sunnix.aje.editor.window.customswing;

import javax.swing.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

public class DefaultValueComboboxModel<E>  extends AbstractListModel<E> implements MutableComboBoxModel<E>, Serializable {
    protected Vector<E> objects;
    protected Object selectedObject;

    protected E defaultValue;

    /**
     * Constructs an empty DefaultValueComboboxModel object.
     */
    public DefaultValueComboboxModel(E defaultValue){
        this.defaultValue = defaultValue;
        this.objects = new Vector<E>();
    }

    /**
     * Constructs a DefaultValueComboboxModel object initialized with
     * an array of objects.
     *
     * @param items  an array of Object objects
     */
    public DefaultValueComboboxModel(E defaultValue, E[] items){
        this.defaultValue = defaultValue;
        objects = new Vector<E>(items.length);

        int i,c;
        for ( i=0,c=items.length;i<c;i++ )
            objects.addElement(items[i]);
    }

    /**
     * Constructs a DefaultValueComboboxModel object initialized with
     * a vector.
     *
     * @param v  a Vector object ...
     */
    public DefaultValueComboboxModel(E defaultValue, Vector<E> v) {
        this.defaultValue = defaultValue;
        objects = v;

        if ( getSize() > 0 ) {
            selectedObject = getElementAt( 0 );
        }
    }

    // implements javax.swing.ComboBoxModel
    /**
     * Set the value of the selected item. The selected item may be null.
     *
     * @param anObject The combo box value or null for no selection.
     */
    public void setSelectedItem(Object anObject) {
        if ((selectedObject != null && !selectedObject.equals( anObject )) ||
                selectedObject == null && anObject != null) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }
    }

    // implements javax.swing.ComboBoxModel
    public Object getSelectedItem() {
        return selectedObject == null ? defaultValue : selectedObject;
    }

    // implements javax.swing.ListModel
    public int getSize() {
        return objects.size() + 1;
    }

    // implements javax.swing.ListModel
    public E getElementAt(int index) {
        if ( index > 0 && index < getSize() )
            return objects.elementAt(index - 1);
        else
            return defaultValue;
    }

    /**
     * Returns the index-position of the specified object in the list.
     *
     * @param anObject the object to return the index of
     * @return an int representing the index position, where 0 is
     *         the first position
     */
    public int getIndexOf(Object anObject) {
        return objects.indexOf(anObject) + 1;
    }

    // implements javax.swing.MutableComboBoxModel
    public void addElement(E anObject) {
        objects.addElement(anObject);
        fireIntervalAdded(this,objects.size(), objects.size());
        if ( objects.size() == 1 && selectedObject == null && anObject != null ) {
            setSelectedItem( anObject );
        }
    }

    // implements javax.swing.MutableComboBoxModel
    public void insertElementAt(E anObject,int index) {
        objects.insertElementAt(anObject,index);
        fireIntervalAdded(this, index + 1, index + 1);
    }

    // implements javax.swing.MutableComboBoxModel
    public void removeElementAt(int index) {
        if(index <= 0)
            return;
        if ( getElementAt( index ) == selectedObject ) {
            setSelectedItem(getElementAt(index - 1));
        }

        objects.removeElementAt(index - 1);

        fireIntervalRemoved(this, index, index);
    }

    // implements javax.swing.MutableComboBoxModel
    public void removeElement(Object anObject) {
        int index = objects.indexOf(anObject);
        if ( index != -1 ) {
            removeElementAt(index + 1);
        }
    }

    /**
     * Empties the list.
     */
    public void removeAllElements() {
        if (!objects.isEmpty()) {
            int firstIndex = 1;
            int lastIndex = objects.size();
            objects.removeAllElements();
            selectedObject = defaultValue;
            fireIntervalRemoved(this, firstIndex, lastIndex);
        } else {
            selectedObject = defaultValue;
        }
    }

    /**
     * Adds all of the elements present in the collection.
     *
     * @param c the collection which contains the elements to add
     * @throws NullPointerException if {@code c} is null
     */
    public void addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return;
        }

        int startIndex = getSize();

        objects.addAll(c);
        fireIntervalAdded(this, startIndex, getSize() - 1);
    }

    /**
     * Adds all of the elements present in the collection, starting
     * from the specified index.
     *
     * @param index index at which to insert the first element from the
     * specified collection
     * @param c the collection which contains the elements to add
     * @throws ArrayIndexOutOfBoundsException if {@code index} does not
     * fall within the range of number of elements currently held
     * @throws NullPointerException if {@code c} is null
     */
    public void addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > getSize()) {
            throw new ArrayIndexOutOfBoundsException("index out of range: " +
                    index);
        }

        if (c.isEmpty()) {
            return;
        }

        objects.addAll(index, c);
        fireIntervalAdded(this, index + 1, index + 1 + c.size() - 1);
    }

}
