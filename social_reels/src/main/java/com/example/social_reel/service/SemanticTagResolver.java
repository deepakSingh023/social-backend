package com.example.social_reel.service;

import com.example.social_reel.entity.TagMapping;
import com.example.social_reel.repository.TagMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SemanticTagResolver {

    private final TagMappingRepository repository;

    public Set<String> resolve(List<String> rawTags) {

        Set<String> semanticTags = new HashSet<>();

        for (String raw : rawTags) {
            String normalized = raw.toLowerCase().trim();

            var mappingOpt = repository.findByRawTag(normalized);

            if (mappingOpt.isPresent()) {
                var mapping = mappingOpt.get();

                // Only add if semantic exists
                if (mapping.getSemanticTag() != null) {
                    semanticTags.add(mapping.getSemanticTag());
                }

            } else {
                // 🔥 Create new tag with null semantic
                repository.save(
                        TagMapping.builder()
                                .rawTag(normalized)
                                .semanticTag(null)
                                .build()
                );
            }
        }

        return semanticTags;
    }
}


