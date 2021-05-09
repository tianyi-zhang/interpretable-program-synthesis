package edu.harvard.seas.synthesis.sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaxExamplesSampler extends RegexSampler {
    private static final int Max_Regex = 10;

    public MaxExamplesSampler(List<String> logFiles) {
        super(logFiles);
    }

    @Override
    public Set<String> sample() {
        HashMap<Integer, Set<String>> example_map = new HashMap<>();
        int max_size = -1;
        for(String ex: this.match_map.keySet()){
            // for each key
            int size = this.match_map.get(ex).size();

            max_size = size > max_size ? size : max_size;

            if(!example_map.containsKey(size)){
                example_map.put(size, new HashSet<>());
            }

            example_map.get(size).add(ex);
        }

        if(max_size > 0) {
//            return example_map.get(max_size);
            return Stream.of(example_map.get(max_size)).flatMap(Set::stream).limit(Max_Regex).collect(Collectors.toSet());
        }

        return new HashSet<>();
    }
}
