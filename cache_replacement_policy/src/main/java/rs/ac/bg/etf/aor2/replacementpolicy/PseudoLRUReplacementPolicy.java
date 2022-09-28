package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;

public class PseudoLRUReplacementPolicy implements IReplacementPolicy {
    protected ICacheMemory ICacheMemory;
    protected int[] LRUCnts;
    protected int setAsoc;

    public PseudoLRUReplacementPolicy() {
        LRUCnts = new int[1];
        LRUCnts[0] = 0;
    }

    public void init(ICacheMemory c) {
        this.ICacheMemory = c;
        setAsoc = (int) c.getSetAsociativity();
        int size = (int) ICacheMemory.getSetNum();

        LRUCnts = new int[size];

        for (int i = 0; i < size; i++) {
            LRUCnts[i] = 0;
        }
    }

    public int getBlockIndexToReplace(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        return set * setAsoc + getEntry(adr);
    }


    private int getEntry(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        int result = 0;
        for (int i = 0; i < setAsoc; i++) {
            int block = set * setAsoc + i;
            Tag tag = tagMemory.get(block);
            if (!tag.V) {
                return i;
            }
        }
        int LRUCnt = LRUCnts[set];
        return get_result(LRUCnt,setAsoc);
    }

    @Override
    public void doOperation(MemoryOperation operation) {

        MemoryOperation.MemoryOperationType opr = operation.getType();

        if ((opr == MemoryOperation.MemoryOperationType.READ)
                || (opr == MemoryOperation.MemoryOperationType.WRITE)) {

            long adr = operation.getAddress();
            int set = (int) ICacheMemory.extractSet(adr);
            long tagTag = ICacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = ICacheMemory.getTags();
            int entry = 0;
            for (int i = 0; i < setAsoc; i++) {
                int block = set * setAsoc + i;
                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            int LRUCnt = LRUCnts[set];
            LRUCnt = entrySelection(LRUCnt,entry,setAsoc);
            LRUCnts[set] = LRUCnt;

        } else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            for (int i = 0; i < LRUCnts.length; i++) {
                LRUCnts[i] = 0;
            }

        }
    }

    int entrySelection(int LRUCnt, int entry, int associativity){
        switch (associativity){
            case 4 ->{
                LRUCnt = LRUCnt & 7;
                switch (entry) {
                    case 0:
                        LRUCnt = LRUCnt & 2;
                        break;
                    case 1:
                        LRUCnt = (LRUCnt & 2) | 1;
                        break;
                    case 2:
                        LRUCnt = (LRUCnt & 1) | 4;
                        break;
                    case 3:
                        LRUCnt = (LRUCnt & 1) | 6;
                        break;
                }
            }
            case 8 ->{
                switch (entry){
                    case 0 ->{
                        LRUCnt &= 0b0101110;
                    }
                    case 1 ->{
                        LRUCnt &= 0b0101110;
                        LRUCnt |= 1;
                    }
                    case 2 ->{
                        LRUCnt &= 0b0101101;
                        LRUCnt |= 0b0010000;
                    }
                    case 3 ->{
                        LRUCnt &= 0b0101101;
                        LRUCnt |= 0b0010010;
                    }
                    case 4 ->{
                        LRUCnt &= 0b0011011;
                        LRUCnt |= 0b1000000;
                    }
                    case 5 ->{
                        LRUCnt &= 0b0011011;
                        LRUCnt |= 0b1000100;
                    }
                    case 6 ->{
                        LRUCnt &= 0b0010111;
                        LRUCnt |= 0b1100000;
                    }
                    case 7 ->{
                        LRUCnt &= 0b0010111;
                        LRUCnt |= 0b1101000;
                    }
                }
            }
            case 16 ->{
                switch (entry){
                    case 0 ->{
                        LRUCnt &= 0b0101110111110;
                    }
                    case 1 ->{
                        LRUCnt &= 0b0101110111110;
                        LRUCnt &= 0b000000000000001;
                    }
                    case 2 ->{
                        LRUCnt &= 0b010111011111101;
                        LRUCnt |= 0b000000100000000;
                    }
                    case 3 ->{
                        LRUCnt &= 0b010111011111101;
                        LRUCnt |= 0b000000100000010;
                    }
                    case 4 ->{
                        LRUCnt &= 0b010110111111011;
                        LRUCnt |= 0b001000000000000;
                    }
                    case 5 ->{
                        LRUCnt &= 0b010110111111011;
                        LRUCnt |= 0b001000000000100;
                    }
                    case 6 ->{
                        LRUCnt &= 0b010110111110111;
                        LRUCnt |= 0b001001000000000;
                    }
                    case 7 ->{
                        LRUCnt &= 0b010110111110111;
                        LRUCnt |= 0b001001000001000;
                    }
                    case 8 ->{
                        LRUCnt &= 0b001101111101111;
                        LRUCnt |= 0b100000000000000;
                    }
                    case 9 ->{
                        LRUCnt &= 0b001101111101111;
                        LRUCnt |= 0b100000000010000;
                    }
                    case 10 ->{
                        LRUCnt &= 0b001101111011111;
                        LRUCnt |= 0b100010000000000;
                    }
                    case 11 ->{
                        LRUCnt &= 0b001101111011111;
                        LRUCnt |= 0b100010000100000;
                    }
                    case 12 ->{
                        LRUCnt &= 0b001011110111111;
                        LRUCnt |= 0b110000000000000;
                    }
                    case 13 ->{
                        LRUCnt &= 0b001011110111111;
                        LRUCnt |= 0b110000001000000;
                    }
                    case 14 ->{
                        LRUCnt &= 0b001011101111111;
                        LRUCnt |= 0b110100000000000;
                    }
                    case 15 ->{
                        LRUCnt &= 0b001011101111111;
                        LRUCnt |= 0b110100010000000;
                    }
                }
            }
            case 32 ->{
                switch (entry){
                    case 0 ->{
                        LRUCnt &= 0b0101110111111101111111111111110;
                    }
                    case 1 ->{
                        LRUCnt &= 0b0101110111111101111111111111110;
                        LRUCnt |= 0b0000000000000000000000000000001;
                    }
                    case 2 ->{
                        LRUCnt &= 0b0101110111111101111111111111101;
                        LRUCnt |= 0b0000000000000010000000000000000;
                    }
                    case 3 ->{
                        LRUCnt &= 0b0101110111111101111111111111101;
                        LRUCnt |= 0b0000000000000010000000000000010;
                    }
                    case 4 ->{
                        LRUCnt &= 0b0101110111111011111111111111011;
                        LRUCnt |= 0b0000001000000000000000000000000;
                    }
                    case 5 ->{
                        LRUCnt &= 0b0101110111111011111111111111011;
                        LRUCnt |= 0b0000001000000000000000000000100;
                    }
                    case 6 ->{
                        LRUCnt &= 0b0101110111111011111111111110111;
                        LRUCnt |= 0b0000001000000100000000000000000;
                    }
                    case 7 ->{
                        LRUCnt &= 0b0101110111111011111111111110111;
                        LRUCnt |= 0b0000001000000100000000000001000;
                    }
                    case 8 ->{
                        LRUCnt &= 0b0101101111110111111111111101111;
                        LRUCnt |= 0b0010000000000000000000000000000;
                    }
                    case 9 ->{
                        LRUCnt &= 0b0101101111110111111111111101111;
                        LRUCnt |= 0b0010000000000000000000000010000;
                    }
                    case 10 ->{
                        LRUCnt &= 0b0101101111110111111111111011111;
                        LRUCnt |= 0b0010000000001000000000000000000;
                    }
                    case 11 ->{
                        LRUCnt &= 0b0101101111110111111111111011111;
                        LRUCnt |= 0b0010000000001000000000000100000;
                    }
                    case 12 ->{
                        LRUCnt &= 0b0101101111101111111111110111111;
                        LRUCnt |= 0b0010010000000000000000000000000;
                    }
                    case 13 ->{
                        LRUCnt &= 0b0101101111101111111111110111111;
                        LRUCnt |= 0b0010010000000000000000001000000;
                    }
                    case 14 ->{
                        LRUCnt &= 0b0101101111101111111111101111111;
                        LRUCnt |= 0b0010010000010000000000000000000;
                    }
                    case 15 ->{
                        LRUCnt &= 0b0101101111101111111111101111111;
                        LRUCnt |= 0b0010010000010000000000010000000;
                    }
                    case 16 ->{
                        LRUCnt &= 0b0011011111011111111111011111111;
                        LRUCnt |= 0b1000000000000000000000000000000;
                    }
                    case 17 ->{
                        LRUCnt &= 0b0011011111011111111111011111111;
                        LRUCnt |= 0b1000000000000000000000100000000;
                    }
                    case 18 ->{
                        LRUCnt &= 0b0011011111011111111110111111111;
                        LRUCnt |= 0b1000000000100000000000000000000;
                    }
                    case 19 ->{
                        LRUCnt &= 0b0011011111011111111110111111111;
                        LRUCnt |= 0b1000000000100000000001000000000;
                    }
                    case 20 ->{
                        LRUCnt &= 0b0011011110111111111101111111111;
                        LRUCnt |= 0b1000100000000000000000000000000;
                    }
                    case 21 ->{
                        LRUCnt &= 0b0011011110111111111101111111111;
                        LRUCnt |= 0b1000100000000000000010000000000;
                    }
                    case 22 ->{
                        LRUCnt &= 0b0011011110111111111011111111111;
                        LRUCnt |= 0b1000100001000000000000000000000;
                    }
                    case 23 ->{
                        LRUCnt &= 0b0011011110111111111011111111111;
                        LRUCnt |= 0b1000100001000000000100000000000;
                    }
                    case 24 ->{
                        LRUCnt &= 0b0010111101111111110111111111111;
                        LRUCnt |= 0b1100000000000000000000000000000;
                    }
                    case 25 ->{
                        LRUCnt &= 0b0010111101111111110111111111111;
                        LRUCnt |= 0b1100000000000000001000000000000;
                    }
                    case 26 ->{
                        LRUCnt &= 0b0010111101111111101111111111111;
                        LRUCnt |= 0b1100000010000000000000000000000;
                    }
                    case 27 ->{
                        LRUCnt &= 0b0010111101111111101111111111111;
                        LRUCnt |= 0b1100000010000000010000000000000;
                    }
                    case 28 ->{
                        LRUCnt &= 0b0010111011111111011111111111111;
                        LRUCnt |= 0b1101000000000000000000000000000;
                    }
                    case 29 ->{
                        LRUCnt &= 0b0010111011111111011111111111111;
                        LRUCnt |= 0b1101000000000000100000000000000;
                    }
                    case 30 ->{
                        LRUCnt &= 0b0010111011111110111111111111111;
                        LRUCnt |= 0b1101000100000000000000000000000;
                    }
                    case 31 ->{
                        LRUCnt &= 0b0010111011111110111111111111111;
                        LRUCnt |= 0b1101000100000001000000000000000;
                    }
                }
            }
        }
        return LRUCnt;
    }

    String toBinary(int x, int len){
        if(len > 0){
            return String.format("%"+len+"s",Integer.toBinaryString(x)).replaceAll(" ","0");
        }
        return null;
    }
    int get_result(int LRUCnt, int associativity){
        int first_bit = (int) (Math.random()%2);
        String[] bits;
        int iter_index = 0;
        switch (associativity){
            case 4 ->{
//                LRUCnt |= first_bit<<2;
                bits = toBinary(LRUCnt,3).split("");
                for(int i = 0;i<1;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=1;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-1)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-1)*2);
                }
            }
            case 8 ->{
//                LRUCnt |= first_bit<<6;
                bits = toBinary(LRUCnt,7).split("");
                for(int i = 0;i<2;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=3;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-3)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-3)*2);
                }

            }
            case 16 ->{
//                LRUCnt |= first_bit<<14;
                bits = toBinary(LRUCnt,15).split("");
                for(int i = 0;i<3;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=7;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-7)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-7)*2);
                }

            }
            case 32 ->{
//                LRUCnt |= first_bit<<30;
                bits = toBinary(LRUCnt,31).split("");
                for(int i = 0;i<4;i++){
                    if(bits[iter_index].equals("0")){
                        iter_index = iter_index*2 + 1;
                    }
                    else {
                        iter_index = iter_index*2 + 2;
                    }
                }
                String iter_bit = bits[iter_index];
                iter_index-=15;
                if(iter_bit.equals("0")){
                    return (Math.abs(iter_index-15)*2)+1;
                }
                else {
                    return (Math.abs(iter_index-15)*2);
                }

            }
        }
        return -1;
    }

    public String printAll() {
        String s = "";
        int size = LRUCnts.length;
        for (int i = 0; i < size; i++) {
            s = s + "Set " + i + ", Pseudo LRU counter " + LRUCnts[i] + "\n";
        }
        return s;
    }

    @Override
    public void reset() {
        for (int i = 0; i < LRUCnts.length; i++) {
            LRUCnts[i] = 0;
        }
    }

    public String printValid() {
        String s = "";
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int setNumber = (int) ICacheMemory.getSetNum();
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        for (int set = 0; set < setNumber; set++) {
            boolean valid = false;
            for (int j = 0; j < setAsoc; j++) {
                int block = set * setAsoc + j;
                Tag tag = tagMemory.get(block);
                if (tag.V) {
                    valid = true;
                }
            }
            if (valid) {
                s = s + "Set " + set + ", Pseudo LRU counter " + LRUCnts[set]
                        + "\n";
            }
        }
        return s;
    }
}
