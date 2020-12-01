import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TwentyFour_BTree<Key extends Comparable<Key>>  {
    //change order here
    private static final int M = 24;
    private Node root;       
    private int height;      
    private int n;           

    //nodes class
    private static final class Node{
        //num of children
        private int c;                    
        //array of children        
        private Entry[] children = new Entry[M];   
        private Node(int x){
            c = x;
        }
    }

    private static class Entry{
        private Comparable key;
        private Node next;     
        public Entry(Comparable key, Node next) {
            this.key  = key;
            this.next = next;
        }
    }

    //empty initializer
    public TwentyFour_BTree(){
        root = new Node(0);
    }
 
    //helper functions
    public boolean isEmpty(){
        return size() == 0;
    }
    public int height(){
        return height;
    }
    public int size(){
        return n;
    }
    private boolean less_than(Comparable k1, Comparable k2){
        return k1.compareTo(k2) < 0;
    }

    private boolean equal_to(Comparable k1, Comparable k2){
        return k1.compareTo(k2) == 0;
    }
    //split node in half
    private Node split(Node h){
        Node t = new Node(M/2);
        h.c = M/2;
        for (int j = 0; j < M/2; j++)
            t.children[j] = h.children[M/2+j]; 
        return t;    
    }

    //adding to tree
    public void put(Key key){
        if (key == null) throw new IllegalArgumentException("argument is not valid; null");
        Node u = insert(root, key, height); 
        n++;
        if (u == null) return;
        //split if inserted is not null
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, root);
        t.children[1] = new Entry(u.children[0].key, u);
        root = t;
        height++;
    }

    private Node insert(Node h, Key key, int height){
        int j;
        Entry t = new Entry(key, null);
        //if reached level 0
        if(height == 0){
            for(j = 0; j < h.c; j++){
                if(less_than(key, h.children[j].key)) break;
            }
        }
        else{
            for(j = 0; j < h.c; j++){
                if((j+1 == h.c) || less_than(key, h.children[j+1].key)) {
                    System.out.println(h.children[j].key);
                    Node u = insert(h.children[j++].next, key, height-1);
                    if (u == null) return null;
                    t.key = u.children[0].key;
                    t.next = u;
                    break;
                }
            }
        }
        for (int i = h.c; i > j; i--){
            h.children[i] = h.children[i-1];
        }
        h.children[j] = t;
        h.c++;
        if(h.c < M) return null;
        else return split(h);
    }

    public boolean get(Key key){
        if(key == null) throw new IllegalArgumentException("argument is not valid; null");
        return search(root, key, height);
    }

    //recursive searching
    private boolean search(Node x, Key key, int height){
        Entry[] children = x.children;
        if(height == 0){
            for(int j = 0; j < x.c; j++){
                if(equal_to(key, children[j].key)) return true;
            }
        }
        else{
            for(int j = 0; j < x.c; j++){
                if(j+1 == x.c || less_than(key, children[j+1].key)){
                    System.out.println(children[j].key);
                    return search(children[j].next, key, height-1);
                }
                    
            }
        }
        return false;
    }

    public void delete(Key key){
        if(key == null) throw new IllegalArgumentException("argument is not valid; null");
        if(search(root, key, height)) System.out.println("Key was found and deleted!");
        else System.out.println("Key was not found, and thus not deleted");
    }

    //convert tree to string for outputting
    public String toString() {
        return toString(root, height, "") + "\n";
    }
    private String toString(Node h, int ht, String indent){
        StringBuilder sb = new StringBuilder();
        Entry[] children = h.children;

        if (ht == 0){
            for (int j = 0; j < h.c; j++) {
                sb.append(indent + children[j].key + "\n");
            }
        }
        else{
            for(int j = 0; j < h.c; j++){
                if (j > 0) sb.append(indent + "(" + children[j].key + ")\n");
                sb.append(toString(children[j].next, ht-1, indent + "     "));
            }
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        TwentyFour_BTree<String> bt = new TwentyFour_BTree<String>();
        Set<Integer> check = new HashSet<Integer>();
        Random rand = new Random();
        System.out.println("Starting data generation...");
        System.out.println();
        while(check.size() < 10000){
            int randInt = rand.nextInt(100000) + 100000;
            if(check.add(randInt)) bt.put(String.valueOf(randInt));
        }
        System.out.println("Data Generation Done!");
        System.out.println();
        System.out.println("size:    " + bt.size());
        System.out.println("height:  " + bt.height());
        System.out.println();

        //randomn insertion and deletion operations
        int i = 0;
        while(i < 5){
            int randInt = rand.nextInt(100000) + 100000;
            if(check.add(randInt)){
                System.out.println("Inserting "+randInt+"...");
                i++;
                System.out.println("Nodes involved: ");
                bt.put(String.valueOf(randInt));
                System.out.println();
            }
        }
        while(i > 0){
            int randInt = rand.nextInt(100000) + 100000;
            if(check.remove(randInt)){
                System.out.println("Deleting "+randInt+"...");
                i--;
                bt.delete(String.valueOf(randInt));
                System.out.println();
            }
        }

        //random search operations, guaranteeing at least three finds
        while(i < 3){
            int randInt = rand.nextInt(100000) + 100000;
            System.out.println("Searching for "+randInt+"...");
            System.out.println("Nodes involved: ");
            boolean isThere = bt.get(String.valueOf(randInt));
            if(isThere){
                System.out.println("Found "+randInt+"!");
                i++;
            }
            else System.out.println(randInt+" does not exist in this tree.");
            System.out.println();
        }
    }

}
