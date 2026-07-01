package com.mitocode.shared.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableFactory {

    private PageableFactory() {
    }

    public static <T extends Enum<T> & SortableField> Pageable of(int page, int size, T sortBy, Sort.Direction direction) {
        return sortBy == null
                ? PageRequest.of(page, size)
                : PageRequest.of(page, size, Sort.by(direction, sortBy.getProperty()));
    }
}
