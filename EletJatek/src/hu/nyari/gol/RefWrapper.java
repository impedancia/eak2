package hu.nyari.gol;

public class RefWrapper<E> {
    E ref;
    public RefWrapper(E e){
        ref = e;
    }
    public E get() {return ref;}
    public void set(E e) {this.ref = e;};
}
