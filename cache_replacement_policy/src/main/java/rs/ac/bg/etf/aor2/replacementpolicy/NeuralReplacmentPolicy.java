package rs.ac.bg.etf.aor2.replacementpolicy;

import java.io.IOException;
import java.util.ArrayList;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

public class NeuralReplacmentPolicy implements IReplacementPolicy{
 


    private boolean training = false;
    private String ip;
    private ICacheMemory iCacheMemory;



    private ArrayList<Integer> number_of_set_accesses;
    private ArrayList<Integer> set_accesses_since_miss;
    private ArrayList<Integer> set_accesses_since_cache_line_insertion;
    private ArrayList<Integer> set_accesses_since_cache_line_access;
    private ArrayList<Integer> cache_line_hits_since_insertion;

    private ArrayList<Integer> preuse_access_information;
    private ArrayList<ArrayList<Integer>> preuse_cache_line_information;

    

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

        preuse_access_information = new ArrayList<Integer>(set_num);
        preuse_cache_line_information = new ArrayList<ArrayList<Integer>>(set_num);

        for(int i = 0;i < set_num;i++){
            preuse.add(new ArrayList<Integer>(set_asoc));
        }
        
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        // TODO Auto-generated method stub
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
                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            



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

    public int reuse_distance(){
        return 0;
    }



}
