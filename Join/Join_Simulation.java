import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class Join_Simulation{

    //global variable  
    public static final int NUM_OF_BLOCKS_IN_MEMORY = 15; 

    //tuple class
    public static class Tuple implements Serializable{
        private ArrayList<Integer> fields;
        private int relation_index;
        public Tuple(){
            this.fields = new ArrayList<Integer>();
            this.relation_index = -1;
        }
        public Tuple(Tuple t){
            this.fields = t.getFields();
            this.relation_index = t.getRelIndex();
        }
        public Tuple(int index){
            this.fields = new ArrayList<>();
            this.relation_index = index;
        }
        public void setFields(int a, int b){
            this.fields.add(a);
            this.fields.add(b);
        }
        public void setFields(int a, int b, int c){
            this.fields.add(a);
            this.fields.add(b);
            this.fields.add(c);
        }
        public ArrayList<Integer> getFields(){
            return this.fields;
        }
        public int getRelIndex(){
            return this.relation_index;
        }
        public void setRelIndex(int i){
            this.relation_index = i;
        }
        public void erase(){
            this.fields.clear();
        }
    }

    //block class
    public static class Block implements Serializable{
        private ArrayList<Tuple> tuples;

        public Block(){
            this.tuples = new ArrayList<Tuple>();
        } 

        public Block(Block b){
            this.tuples = b.getTuples();
        }
        public boolean isFull(){
            if(tuples.size()==0) return false;
            if(tuples.size() == 8) return true;
            return false;
        }
        public boolean addTuple(Tuple tuple) {
            if (isFull()) {
              System.err.print("ERROR: the block is full" + "\n");
              return false;
            }
            this.tuples.add(new Tuple(tuple));
            return true;
        }
	    public ArrayList<Tuple> getTuples(){
            return this.tuples;
        }
        public boolean setTuples(ArrayList<Tuple> tuples, int start, int end) {
            if((end - start) > 8) {
                System.err.print("exceed space limit of the block" + "\n");
                return false;
            }
            this.tuples.clear();
            for(int i=start; i<end; i++){
                this.tuples.add(new Tuple(tuples.get(i)));
            }
            return true;
        }
    }

    //virtual disk
    public static class Disk implements Serializable{
        private ArrayList<ArrayList<Block>> tracks;
        private long diskIOs;

        public Disk(){
            this.tracks = new ArrayList<ArrayList<Block>>();
            for(int i = 0; i < 5; i++){
                this.tracks.add(new ArrayList<Block>());
            }
            this.diskIOs = 0;
        }
        public void incrementDiskIO(int x){
            this.diskIOs += x;
        }
        public long getDiskIO(){
            return this.diskIOs;
        }
        public void resetDiskIO(){
            this.diskIOs = 0;
        }
        public boolean setBlock(int relation_index, int index, Block b){
            incrementDiskIO(1);
            this.tracks.get(relation_index).add(b);
            return true;
        }
        public ArrayList<ArrayList<Block>> getTracks(){
            return this.tracks;
        }
        public Block getBlock(int rel_index, int block_index) {
            if(block_index<0 || block_index >= this.tracks.get(rel_index).size())  {
              System.err.print("block index " + block_index + " out of disk bound" + "\n");
              return new Block();
            }
            incrementDiskIO(1);
            return new Block(this.tracks.get(rel_index).get(block_index));
        }
        public boolean extendTrack(int relation_index, int index, Tuple t){
            if(index < 0){
                System.err.print("Block index is out of bounds.");
                return false;
            }
            ArrayList<Block> track = tracks.get(relation_index);
            int j = track.size();
            if(index > j){
                if(j > 0){
                    while(!track.get(j-1).isFull()){
                        track.get(j-1).addTuple(new Tuple(t));
                    }
                }
                for(int i = j; i < index-1; i++) {
                    track.add(new Block());
                    while(!track.get(i).isFull()){
                      track.get(i).addTuple(new Tuple(t));
                    }
                }
                track.add(new Block());
                track.get(index-1).addTuple(new Tuple(t));
            }
            return true;
        }
        public boolean retractTrack(int rel_index, int block_index) {
            if(block_index < 0 || block_index >= tracks.get(rel_index).size()){
              System.err.print("out of disk bound" + "\n");
              return false;
            }
            tracks.get(rel_index).subList(block_index, tracks.get(rel_index).size()).clear();
            return true;
        }

    }

    //virtual memory
    public static class Memory implements Serializable{
        private Block[] blocks;

        public Memory(){
            blocks = new Block[NUM_OF_BLOCKS_IN_MEMORY];
            for(int i = 0; i < NUM_OF_BLOCKS_IN_MEMORY; i++){
                blocks[i] = new Block();
            }
        }
        public int getMemorySize(){
            return NUM_OF_BLOCKS_IN_MEMORY;
        }
        public Block getBlock(int index){
            if(index < 0 || index > NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("Block index out of bounds.");
                return null;
            }
            return blocks[index];
        }
        public void setBlock(int index, Block b){
            if(index < 0 || index > NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("Block index out of bounds.");
                return;
            }
            blocks[index] = b;           
        }
        public ArrayList<Tuple> getTuples(int start, int num_blocks){
            if(start < 0 || start > NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("Block index out of bounds.");
                return new ArrayList<Tuple>();
            }
            if(num_blocks <= 0){
                System.err.print("Not enough blocks");
                return new ArrayList<Tuple>();
            }
            ArrayList<Tuple> all = new ArrayList<Tuple>();
            for(int i = start; i < start+num_blocks; i++){
                ArrayList<Tuple> temp = blocks[i].getTuples();
                for(Tuple t : temp){
                    all.add(t);
                }
            }
            return all;
        }
        public boolean setTuples(int start, ArrayList<Tuple> tuples){
            if(start < 0 || start > NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("Block index out of bounds.");
                return false;
            }
            int numBlocks = tuples.size()/8;
            int additional = 0;
            if(tuples.size() % 8 != 0){
                additional = 1;
            }
            if(start+numBlocks+additional > NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("Not enough memory space.");
                return false;
            }
            int w1 = 0, w2 = 0, i;
            for(i = start; i < start + numBlocks; i++){
                w2 += 8;
                blocks[i].setTuples(tuples, w1, w2);
                w1 = w2;
            }
            if(additional != 0){
                blocks[i].setTuples(tuples, w1, tuples.size());
            }
            return true;
        }
    }

    //relation class
    public static class Relation implements Serializable{
        private Disk disk;
        private Memory mem;
        private String name;
        private int index;

        public Relation(Disk d, Memory m, String name, int index){
            this.disk = d;
            this.mem = m;
            this.name = name;
            this.index = index;
        }
        public int getNumBlocks(){
            return disk.getTracks().get(this.index).size();
        }
        public boolean getBlock(int block_index){
            if(block_index < 0 || block_index >= NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("block index " + block_index + " out of bound in memory" + "\n");
                return false;
            }
            Block b = disk.getBlock(this.index, block_index);
            if(!b.getTuples().isEmpty()) {
                mem.setBlock(block_index, b);
                return true;
            }
            return false;
        }
        public boolean setBlock(int rel_index, int block_index){
            if (block_index<0 || block_index >= NUM_OF_BLOCKS_IN_MEMORY){
                System.err.print("block index" + block_index + " out of bound in memory" + "\n");
                return false;
            }
            if(rel_index<0){
                System.err.print("block index " + rel_index + " out of bound in relation" + "\n");
                return false;
            }
            Tuple t = new Tuple(index);
            t.erase();
            if (disk.extendTrack(index,rel_index+1,t)){
                return disk.setBlock(index, rel_index, mem.getBlock(block_index));
            }
            return false;
        }
        public boolean deleteBlocks(int startindex){
            return disk.retractTrack(index, startindex);
        }
    }

    //main class
    public static void main(String[] args){
        System.out.println("Starting components...");
        System.out.println();
        Memory mem = new Memory();
        Disk disk = new Disk();

        System.out.println("Setting up relation S(B, C)...");
        System.out.println();
        //creating relation S(B, C)
        Relation s = new Relation(disk, mem, "S(B,C)", 0);
        //adding tuples to S's tuples to disk
        Random rand = new Random();
        Block b = new Block();
        int blockindex = 0;
        Set<Integer> primary = new HashSet<>();
        int i = 0;
        while(i < 5000){
            int randInt = rand.nextInt(40000) + 10000;
            if(primary.add(randInt)){
                int randInt2 = rand.nextInt();
                Tuple t = new Tuple(0);
                t.setFields(randInt, randInt2);
                if(b.isFull()){
                    disk.setBlock(0, blockindex, b);
                    blockindex++;
                    b = new Block();
                }
                else{
                    b.addTuple(t);
                    i++;
                }
            }
        }

        System.out.println("Setting up relation R1(A, B) with 1000 tuples...");
        System.out.println();
        //creating relation R1(A, B)
        Relation r1 = new Relation(disk, mem, "R1(A,B)", 1);
        b = new Block();
        i = 0;
        int offset = primary.size();
        ArrayList<Integer> c = new ArrayList<Integer>(primary);
        blockindex = 0;
        //loading this into disk, making sure some of the B values match with S relation
        while(i < 500){
            int randInt = c.get(rand.nextInt(offset));
            int randInt2 = rand.nextInt();
            Tuple t = new Tuple(1);
            t.setFields(randInt2, randInt);
            if(b.isFull()){
                disk.setBlock(1, blockindex, b);
                blockindex++;
                b = new Block();
            }
            else{
                b.addTuple(t);
                i++;
            }
        }
        while(i < 500){
            int randInt = rand.nextInt();
            int randInt2 = rand.nextInt();
            Tuple t = new Tuple(1);
            t.setFields(randInt, randInt2);
            if(b.isFull()){
                disk.setBlock(1, blockindex, b);
                blockindex++;
                b = new Block();
            }
            else{
                b.addTuple(t);
                i++;
            }
        }

        System.out.println("Loading into memory from disk and computing JOIN(R1, S), and loading back into disk...");
        System.out.println();

        //loading R1 into memory
        b = new Block();
        blockindex = 0;
        for(int j = 0; j < disk.getTracks().get(1).size(); j++){
            mem.setBlock(0, disk.getBlock(1, j));
            for(int k = 0; k < disk.getTracks().get(0).size(); k++){
                mem.setBlock(1, disk.getBlock(0, k));
                ArrayList<Tuple> t1 = mem.getBlock(0).getTuples();
                ArrayList<Tuple> t2 = mem.getBlock(1).getTuples();
                for(int x = 0; x < t1.size(); x++){
                    for(int y = 0; y < t2.size(); y++){
                        if(t1.get(x).getFields().get(1) == t2.get(y).getFields().get(0)){
                            Tuple t = new Tuple(2);
                            t.setFields(t1.get(x).getFields().get(0), t1.get(x).getFields().get(1), t2.get(y).getFields().get(1));
                            if(b.isFull()){
                                disk.setBlock(2, blockindex, b);
                                blockindex++;
                                b = new Block();
                            }
                            else{
                                b.addTuple(t);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Number of Disk IOs for JOIN(R1, S) with 1000 tuples in R1...");
        System.out.println(disk.getDiskIO());
        System.out.println();

        disk.resetDiskIO();

        //creating relation R2(A, B) for part 2
        Relation r2 = new Relation(disk, mem, "R2(A,B)", 3);
        b = new Block();
        i = 0;
        blockindex = 0;
        Random rand2 = new Random();
        //loading this into disk, no need to match with B values in S
        while(i < 1200){
            int randInt = rand2.nextInt();
            int randInt2 = rand2.nextInt();
            Tuple t = new Tuple(3);
            t.setFields(randInt, randInt2);
            if(b.isFull()){
                disk.setBlock(3, blockindex, b);
                blockindex++;
                b = new Block();
            }
            else{
                b.addTuple(t);
            }
            i++;
        }

        System.out.println("Loading into memory from disk and computing JOIN(R2, S), and loading back into disk...");
        System.out.println();
        //loading R2 into memory
        b = new Block();
        blockindex = 0;
        for(int j = 0; j < disk.getTracks().get(3).size(); j++){
            mem.setBlock(0, disk.getBlock(3, j));
            for(int k = 0; k < disk.getTracks().get(0).size(); k++){
                mem.setBlock(1, disk.getBlock(0, k));
                ArrayList<Tuple> t1 = mem.getBlock(0).getTuples();
                ArrayList<Tuple> t2 = mem.getBlock(1).getTuples();
                for(int x = 0; x < t1.size(); x++){
                    for(int y = 0; y < t2.size(); y++){
                        if(t1.get(x).getFields().get(1) == t2.get(y).getFields().get(0)){
                            System.out.println(t1.get(x).getFields().get(1));
                            Tuple t = new Tuple(3);
                            t.setFields(t1.get(x).getFields().get(0), t1.get(x).getFields().get(1), t2.get(y).getFields().get(1));
                            if(b.isFull()){
                                disk.setBlock(4, blockindex, b);
                                blockindex++;
                                b = new Block();
                            }
                            else{
                                b.addTuple(t);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Number of Disk IOs for JOIN(R2, S) with 1200 tuples in R2...");
        System.out.println(disk.getDiskIO());
        System.out.println();

        disk.resetDiskIO();
    }
}