package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;

import java.util.Set;

public class TagFilter implements OperationFilter {
    private final TagService tagService;

    public TagFilter(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public boolean matches(CompleteOperationRow row, FilterCriteria criteria) {
        Set<String> expandedTags = tagService.expandTags(criteria.getSelectedTags());

        if (expandedTags.isEmpty()) return true;

        return expandedTags.stream().anyMatch(tag ->
                tag.equals(row.getTag1()) || tag.equals(row.getTag2()) || tag.equals(row.getTag3())
        );
    }
}