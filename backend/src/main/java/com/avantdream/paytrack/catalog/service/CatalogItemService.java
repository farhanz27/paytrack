package com.avantdream.paytrack.catalog.service;

import java.util.List;

import com.avantdream.paytrack.catalog.dto.CatalogItemRequest;
import com.avantdream.paytrack.catalog.entity.CatalogItem;

public interface CatalogItemService {

    CatalogItem findById(Long companyId, Long itemId);

    List<CatalogItem> findAll(Long companyId);

    CatalogItem create(Long companyId, CatalogItemRequest request);

    CatalogItem update(Long companyId, Long itemId, CatalogItemRequest request);

    void archive(Long companyId, Long itemId);
}
