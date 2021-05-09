package edu.harvard.seas.synthesis.sample;

import edu.harvard.seas.synthesis.TreeObject;

import java.util.*;

public class UniqueSampler extends RegexSampler {

    TreeObject treeObject;

    private static final int Max_Regex = 10;

    public UniqueSampler(List<String> logFiles, TreeObject treeObject) {
        super(logFiles);
        this.treeObject = treeObject;
    }

    @Override
    public Set<String> sample() {
        ArrayList<String> samples = sampleTreeNode(this.treeObject, Max_Regex);
        return new HashSet<>(samples);
    }

    private ArrayList<String> sampleTreeNode(TreeObject treeObject, int count){
        ArrayList<String> result = new ArrayList<>();
        // base case
        if(count == 0){
            return result;
        }
        else if (treeObject.children.length == 0){
            if(treeObject.leaf_count == 1){
                result.add(treeObject.regex);
                return result;
            }
            return result;
        }
        // Find all the children and sort by leaf_count in desc order
        List<TreeObject> childList = Arrays.asList(treeObject.children);
        Collections.sort(childList, new Comparator<TreeObject>() {
            @Override
            public int compare(TreeObject t1, TreeObject t2) {
                return t2.leaf_count - t1.leaf_count;
            }
        });

        int remaining = count;
        int child_remaining = treeObject.children.length;
        HashMap<TreeObject, Integer> sample_count_dict = new HashMap<TreeObject, Integer>();
        int iteration_count = 0;
        while(remaining != 0 && iteration_count < 3) {
            iteration_count++;
            for (TreeObject child : childList) {
                int assigned_count = sample_count_dict.getOrDefault(child, 0);
                int remaining_leaf = child.leaf_count - assigned_count;

                if (remaining_leaf == 0) {
                    continue;
                }

                int required = (int) Math.ceil(((float) count) / child_remaining);
                int available = Math.min(required, remaining_leaf);

                sample_count_dict.put(child, assigned_count + available);

                remaining = remaining - available;
                child_remaining--;
                if(remaining == 0){
                    break;
                }
            }
        }

        for(TreeObject child: sample_count_dict.keySet()) {
            int assigned_count = sample_count_dict.getOrDefault(child, 0);
            if(assigned_count == 0){
                continue;
            }

            result.addAll(sampleTreeNode(child, assigned_count));
        }

        return result;
    }
}
