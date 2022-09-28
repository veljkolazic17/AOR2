package rs.ac.bg.etf.aor2.replacementpolicy;

import java.io.IOException;
import java.util.ArrayList;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.MemoryOperation.MemoryOperationType;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

public class NeuralReplacmentPolicy implements IReplacementPolicy{
 


    private boolean training = false;
    private String ip;
    private ICacheMemory iCacheMemory;


    // State parameters ( not all from paper are included )
    private int[] number_of_set_accesses;
    private int[] set_accesses_since_miss;
    private int[] set_accesses_since_cache_line_insertion;
    private int[] cache_line_hits_since_insertion;
    private int[] line_preuse;

    private int set_num;
    private int set_asoc;

    public NeuralReplacmentPolicy(String ip){
        this.ip = ip;
    }

    @Override
    public void init(ICacheMemory cacheMemory) {

        this.iCacheMemory = cacheMemory;
        this.set_num = (int)this.iCacheMemory.getSetNum();
        this.set_asoc = (int)this.iCacheMemory.getSetAsociativity();

        number_of_set_accesses = new int[set_num];
        set_accesses_since_miss = new int[set_num];
        set_accesses_since_cache_line_insertion = new int[set_num*set_asoc];
        cache_line_hits_since_insertion = new int[set_num*set_asoc];

        line_preuse = new int[set_num*set_asoc];
        
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        
        

        return 0;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        MemoryOperation.MemoryOperationType opr = operation.getType();

        if ((opr == MemoryOperation.MemoryOperationType.READ) || (opr == MemoryOperation.MemoryOperationType.WRITE)) {
            long adr = operation.getAddress();
            int set = (int) iCacheMemory.extractSet(adr);
            long tagTag = iCacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = iCacheMemory.getTags();
            int entry = 0;
            for (int i = 0; i < this.set_asoc; i++) {
                int block = set * this.set_asoc + i;

                line_preuse[block]++;

                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            // Set parameters to right values
            number_of_set_accesses[set]++;
            set_accesses_since_miss[set]++;
            set_accesses_since_cache_line_insertion[entry]++;
            line_preuse[entry] = 0;
        }
        else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            // Do flush
        }
    }

    @Override
    public String printValid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String printAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }

    public void inc_preuse_line(){
        
    }



}
