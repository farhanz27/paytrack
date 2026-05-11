package com.avantdream.paytrack.customer.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.avantdream.paytrack.customer.dto.CustomerRequest;
import com.avantdream.paytrack.customer.entity.Customer;

public interface CustomerService {

    Customer findById(Long companyId, Long customerId);

    Page<Customer> findAll(Long companyId, Pageable pageable);

    List<Customer> search(Long companyId, String q);

    Customer create(Long companyId, CustomerRequest request);

    Customer update(Long companyId, Long customerId, CustomerRequest request);

    void delete(Long companyId, Long customerId);
}
