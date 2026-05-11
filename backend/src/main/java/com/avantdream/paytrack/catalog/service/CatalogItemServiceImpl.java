package com.avantdream.paytrack.catalog.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avantdream.paytrack.catalog.dto.CatalogItemRequest;
import com.avantdream.paytrack.catalog.entity.CatalogItem;
import com.avantdream.paytrack.catalog.repository.CatalogItemRepository;
import com.avantdream.paytrack.company.entity.Company;
import com.avantdream.paytrack.company.repository.CompanyRepository;
import com.avantdream.paytrack.shared.exception.ResourceNotFoundException;

@Service
public class CatalogItemServiceImpl implements CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;
    private final CompanyRepository companyRepository;

    public CatalogItemServiceImpl(CatalogItemRepository catalogItemRepository, CompanyRepository companyRepository) {
        this.catalogItemRepository = catalogItemRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogItem findById(Long companyId, Long itemId) {
        return catalogItemRepository.findByIdAndCompany_Id(itemId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("CatalogItem", itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogItem> findAll(Long companyId) {
        return catalogItemRepository.findAllByCompany_Id(companyId);
    }

    @Override
    @Transactional
    public CatalogItem create(Long companyId, CatalogItemRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        companyRepository.lockById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        long count = catalogItemRepository.countByCompany_Id(companyId) + 1;
        CatalogItem item = new CatalogItem();
        item.setCompany(company);
        item.setItemCode(String.format("ITM-%05d", count));
        applyRequest(item, request);
        return catalogItemRepository.save(item);
    }

    @Override
    @Transactional
    public CatalogItem update(Long companyId, Long itemId, CatalogItemRequest request) {
        CatalogItem item = findById(companyId, itemId);
        applyRequest(item, request);
        return catalogItemRepository.save(item);
    }

    @Override
    @Transactional
    public void archive(Long companyId, Long itemId) {
        CatalogItem item = findById(companyId, itemId);
        item.setArchivedAt(new Date());
        catalogItemRepository.save(item);
    }

    private void applyRequest(CatalogItem item, CatalogItemRequest r) {
        item.setName(r.getName().trim());
        item.setDescription(r.getDescription());
        item.setPrice(r.getPrice());
    }
}
